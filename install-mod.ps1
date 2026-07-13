param(
    [string]$ModsDir,
    [string]$JarMatch,
    [switch]$SkipBuild,
    [switch]$NoVersionBump
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Read-TextFile {
    param([string]$Path)
    return [System.IO.File]::ReadAllText($Path)
}

function Write-TextFile {
    param(
        [string]$Path,
        [string]$Text
    )
    $utf8NoBom = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllText($Path, $Text, $utf8NoBom)
}

function Read-LocalEnvFile {
    param([string]$Path)

    $values = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        return $values
    }

    foreach ($line in [System.IO.File]::ReadAllLines($Path)) {
        if ($line -match '^\s*$' -or $line -match '^\s*#') {
            continue
        }
        $match = [regex]::Match($line, '^\s*(?<key>[A-Za-z_][A-Za-z0-9_]*)\s*=\s*(?<value>.*?)\s*$')
        if (-not $match.Success) {
            throw "Invalid local env line in '$Path': $line"
        }
        $value = $match.Groups['value'].Value.Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $values[$match.Groups['key'].Value] = $value
    }

    return $values
}

function Get-ConfigValue {
    param(
        [string]$Name,
        [AllowNull()][string]$CliValue,
        [hashtable]$LocalEnv
    )

    if (-not [string]::IsNullOrWhiteSpace($CliValue)) {
        return $CliValue
    }

    $processValue = [Environment]::GetEnvironmentVariable($Name, 'Process')
    if (-not [string]::IsNullOrWhiteSpace($processValue)) {
        return $processValue
    }

    if ($LocalEnv.ContainsKey($Name) -and -not [string]::IsNullOrWhiteSpace($LocalEnv[$Name])) {
        return $LocalEnv[$Name]
    }

    return $null
}

function Get-PropertyValue {
    param(
        [string]$Text,
        [string]$Key
    )

    $escapedKey = [regex]::Escape($Key)
    $match = [regex]::Match($Text, "(?m)^\s*$escapedKey\s*=\s*(?<value>.*?)\s*$")
    if (-not $match.Success) {
        throw "Missing required property '$Key'."
    }
    return $match.Groups['value'].Value.Trim()
}

function Set-PropertyValue {
    param(
        [string]$Text,
        [string]$Key,
        [string]$Value
    )

    $escapedKey = [regex]::Escape($Key)
    $pattern = "(?m)^(?<prefix>\s*$escapedKey\s*=\s*)(?<value>.*?)(?<suffix>\s*)$"
    if (-not [regex]::IsMatch($Text, $pattern)) {
        throw "Missing required property '$Key'."
    }

    return [regex]::Replace(
        $Text,
        $pattern,
        { param($m) "$($m.Groups['prefix'].Value)$Value$($m.Groups['suffix'].Value)" },
        1
    )
}

function Get-BumpedVersion {
    param([string]$Version)

    $match = [regex]::Match($Version, '^(?<prefix>.*?)(?<number>\d+)(?<suffix>\D*)$')
    if (-not $match.Success) {
        throw "Cannot bump version '$Version': no trailing numeric component was found."
    }

    $oldNumber = $match.Groups['number'].Value
    $newNumber = ([int64]$oldNumber + 1).ToString().PadLeft($oldNumber.Length, '0')
    return "$($match.Groups['prefix'].Value)$newNumber$($match.Groups['suffix'].Value)"
}

function Replace-VersionLiteralIfPresent {
    param(
        [string]$Path,
        [string]$OldVersion,
        [string]$NewVersion
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return $false
    }

    $text = Read-TextFile -Path $Path
    if (-not $text.Contains($OldVersion)) {
        return $false
    }

    Write-TextFile -Path $Path -Text ($text.Replace($OldVersion, $NewVersion))
    return $true
}

function Normalize-Name {
    param([AllowNull()][string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return ''
    }

    return [regex]::Replace($Value.ToLowerInvariant(), '[^a-z0-9]', '')
}

function Get-ModLoader {
    param(
        [string]$ProjectRoot,
        [string]$BuildGradleText
    )

    if (Test-Path -LiteralPath (Join-Path $ProjectRoot 'src\main\resources\META-INF\neoforge.mods.toml')) {
        return 'NeoForge'
    }
    if (Test-Path -LiteralPath (Join-Path $ProjectRoot 'src\main\resources\META-INF\mods.toml')) {
        return 'Forge'
    }
    if (Test-Path -LiteralPath (Join-Path $ProjectRoot 'src\main\resources\fabric.mod.json')) {
        return 'Fabric'
    }
    if ($BuildGradleText -match 'net\.neoforged') {
        return 'NeoForge'
    }
    if ($BuildGradleText -match 'net\.minecraftforge') {
        return 'Forge'
    }
    if ($BuildGradleText -match 'fabric-loom') {
        return 'Fabric'
    }

    return 'Unknown'
}

function Get-JarModMetadata {
    param([string]$Path)

    $result = [ordered]@{
        Path = $Path
        MetadataPath = $null
        ModId = $null
        DisplayName = $null
        Version = $null
        ReadError = $null
    }

    try {
        Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction SilentlyContinue
        $zip = [System.IO.Compression.ZipFile]::OpenRead($Path)
        try {
            $entry = $zip.Entries | Where-Object {
                $_.FullName -in @('META-INF/neoforge.mods.toml', 'META-INF/mods.toml', 'fabric.mod.json')
            } | Select-Object -First 1

            if ($null -eq $entry) {
                return [pscustomobject]$result
            }

            $stream = $entry.Open()
            try {
                $reader = [System.IO.StreamReader]::new($stream)
                try {
                    $content = $reader.ReadToEnd()
                }
                finally {
                    $reader.Dispose()
                }
            }
            finally {
                $stream.Dispose()
            }

            $result.MetadataPath = $entry.FullName
            if ($entry.FullName -eq 'fabric.mod.json') {
                $json = $content | ConvertFrom-Json
                $result.ModId = $json.id
                $result.DisplayName = $json.name
                $result.Version = $json.version
            }
            else {
                $match = [regex]::Match($content, '(?m)^\s*modId\s*=\s*"(?<value>[^"]+)"')
                if ($match.Success) { $result.ModId = $match.Groups['value'].Value }
                $match = [regex]::Match($content, '(?m)^\s*displayName\s*=\s*"(?<value>[^"]+)"')
                if ($match.Success) { $result.DisplayName = $match.Groups['value'].Value }
                $match = [regex]::Match($content, '(?m)^\s*version\s*=\s*"(?<value>[^"]+)"')
                if ($match.Success) { $result.Version = $match.Groups['value'].Value }
            }
        }
        finally {
            $zip.Dispose()
        }
    }
    catch {
        $result.ReadError = $_.Exception.Message
    }

    return [pscustomobject]$result
}

function Test-IsExcludedJarName {
    param([string]$Name)

    return $Name -match '(?i)(^|[-.])(sources|javadoc|dev|plain|test|tests|api)([-.]|\.jar$)'
}

function Test-IsSameModJar {
    param(
        [System.IO.FileInfo]$Jar,
        [string]$ProjectModId,
        [string]$ProjectModName,
        [string]$JarNameMatch
    )

    if (-not [string]::IsNullOrWhiteSpace($JarNameMatch) -and $Jar.Name -like $JarNameMatch) {
        return $true
    }

    $metadata = Get-JarModMetadata -Path $Jar.FullName
    if ($metadata.ModId -and $metadata.ModId -eq $ProjectModId) {
        return $true
    }

    $modIdKey = Normalize-Name $ProjectModId
    $modNameKey = Normalize-Name $ProjectModName
    $shortModName = (($ProjectModName -split '[:(]')[0]).Trim()
    $shortModNameKey = Normalize-Name $shortModName
    $baseKey = Normalize-Name $Jar.BaseName

    if ($modIdKey.Length -gt 0 -and $baseKey.Contains($modIdKey)) {
        return $true
    }
    if ($shortModNameKey.Length -ge 4 -and $baseKey.Contains($shortModNameKey)) {
        return $true
    }

    if ($metadata.DisplayName) {
        $displayNameKey = Normalize-Name $metadata.DisplayName
        if ($displayNameKey -eq $modNameKey -or ($shortModNameKey.Length -ge 4 -and $displayNameKey.Contains($shortModNameKey))) {
            return $true
        }
    }

    return $false
}

function Get-VersionDeclarationStatus {
    param(
        [string]$Path,
        [string]$Version
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return [ordered]@{
            Path = $Path
            Status = 'missing'
        }
    }

    $text = Read-TextFile -Path $Path
    $status = 'no-version-reference-found'
    if ($text.Contains($Version)) {
        $status = "literal:$Version"
    }
    elseif ($text.Contains('${mod_version}') -or $text.Contains('mod_version')) {
        $status = 'uses:mod_version'
    }

    return [ordered]@{
        Path = $Path
        Status = $status
    }
}

function Resolve-ConfiguredPath {
    param(
        [string]$ProjectRoot,
        [string]$PathValue
    )

    if ([string]::IsNullOrWhiteSpace($PathValue)) {
        throw 'No Minecraft mods directory configured. Set CODEX_MINECRAFT_MODS_DIR in .codex/local.env or pass -ModsDir.'
    }

    if ([System.IO.Path]::IsPathRooted($PathValue)) {
        return [System.IO.Path]::GetFullPath($PathValue)
    }

    return [System.IO.Path]::GetFullPath((Join-Path $ProjectRoot $PathValue))
}

function Get-SafePathSegment {
    param([string]$Value)

    $safe = [regex]::Replace($Value.ToLowerInvariant(), '[^a-z0-9._-]+', '-').Trim('-')
    if ([string]::IsNullOrWhiteSpace($safe)) {
        return 'project'
    }
    return $safe
}

function Get-GradleBuildDirectory {
    param([string]$ProjectRoot)

    $configured = [Environment]::GetEnvironmentVariable('CODEX_GRADLE_BUILD_DIR', 'Process')
    if (-not [string]::IsNullOrWhiteSpace($configured)) {
        if ([System.IO.Path]::IsPathRooted($configured)) {
            return [System.IO.Path]::GetFullPath($configured)
        }
        return [System.IO.Path]::GetFullPath((Join-Path $ProjectRoot $configured))
    }

    $normalizedProjectRoot = $ProjectRoot.Replace('\', '/').ToLowerInvariant()
    if ($normalizedProjectRoot.Contains('/onedrive/')) {
        $localAppData = [Environment]::GetEnvironmentVariable('LOCALAPPDATA', 'Process')
        if ([string]::IsNullOrWhiteSpace($localAppData)) {
            $localAppData = Join-Path ([Environment]::GetFolderPath('UserProfile')) 'AppData\Local'
        }
        return [System.IO.Path]::GetFullPath((Join-Path $localAppData ("Codex\gradle-builds\" + (Get-SafePathSegment -Value (Split-Path $ProjectRoot -Leaf)))))
    }

    return [System.IO.Path]::GetFullPath((Join-Path $ProjectRoot 'build'))
}

$projectRoot = $PSScriptRoot
$gradleBuildDir = Get-GradleBuildDirectory -ProjectRoot $projectRoot
$env:CODEX_GRADLE_BUILD_DIR = $gradleBuildDir
$gradlePropertiesPath = Join-Path $projectRoot 'gradle.properties'
$buildGradlePath = Join-Path $projectRoot 'build.gradle'
$buildGradleKtsPath = Join-Path $projectRoot 'build.gradle.kts'
$neoforgeTomlPath = Join-Path $projectRoot 'src\main\resources\META-INF\neoforge.mods.toml'
$modsTomlPath = Join-Path $projectRoot 'src\main\resources\META-INF\mods.toml'
$fabricJsonPath = Join-Path $projectRoot 'src\main\resources\fabric.mod.json'
$localEnvPath = Join-Path $projectRoot '.codex\local.env'

if (-not (Test-Path -LiteralPath $gradlePropertiesPath)) {
    throw "gradle.properties was not found at '$gradlePropertiesPath'."
}

$localEnv = Read-LocalEnvFile -Path $localEnvPath
$propertiesText = Read-TextFile -Path $gradlePropertiesPath
$buildGradleText = if (Test-Path -LiteralPath $buildGradlePath) { Read-TextFile -Path $buildGradlePath } else { '' }

$modLoader = Get-ModLoader -ProjectRoot $projectRoot -BuildGradleText $buildGradleText
$modId = Get-PropertyValue -Text $propertiesText -Key 'mod_id'
$modName = Get-PropertyValue -Text $propertiesText -Key 'mod_name'
$previousVersion = Get-PropertyValue -Text $propertiesText -Key 'mod_version'
$newVersion = if ($NoVersionBump) { $previousVersion } else { Get-BumpedVersion -Version $previousVersion }

$defaultModsDir = Join-Path $projectRoot 'runs\client\mods'
$configuredModsDir = Get-ConfigValue -Name 'CODEX_MINECRAFT_MODS_DIR' -CliValue $ModsDir -LocalEnv $localEnv
if ([string]::IsNullOrWhiteSpace($configuredModsDir)) {
    $configuredModsDir = $defaultModsDir
}
$resolvedModsDir = Resolve-ConfiguredPath -ProjectRoot $projectRoot -PathValue $configuredModsDir

$configuredJarMatch = Get-ConfigValue -Name 'MOD_JAR_MATCH' -CliValue $JarMatch -LocalEnv $localEnv
if ([string]::IsNullOrWhiteSpace($configuredJarMatch)) {
    $configuredJarMatch = "$modId*.jar"
}
if ($configuredJarMatch -in @('*', '*.*', '*.jar')) {
    throw "MOD_JAR_MATCH '$configuredJarMatch' is too broad. Use a pattern specific to '$modId'."
}

$literalUpdates = @()
if (-not $NoVersionBump) {
    if ($newVersion -eq $previousVersion) {
        throw 'Version was not incremented.'
    }

    $propertiesText = Set-PropertyValue -Text $propertiesText -Key 'mod_version' -Value $newVersion
    Write-TextFile -Path $gradlePropertiesPath -Text $propertiesText

    foreach ($path in @($buildGradlePath, $buildGradleKtsPath, $neoforgeTomlPath, $modsTomlPath, $fabricJsonPath)) {
        if (Replace-VersionLiteralIfPresent -Path $path -OldVersion $previousVersion -NewVersion $newVersion) {
            $literalUpdates += $path
        }
    }
}

New-Item -ItemType Directory -Force -Path $resolvedModsDir | Out-Null

$gradlew = Join-Path $projectRoot 'gradlew.bat'
$gradleCommand = @()
if ($SkipBuild) {
    $gradleCommand = @('skipped-existing-build')
}
else {
    Push-Location $projectRoot
    try {
        if (Test-Path -LiteralPath $gradlew) {
            $gradleCommand = @('.\gradlew.bat', 'clean', 'build')
            & $gradlew clean build
        }
        else {
            $gradleCommand = @('gradle', 'clean', 'build')
            & gradle clean build
        }

        if ($LASTEXITCODE -ne 0) {
            throw "Gradle build failed with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
}

$libsDir = Join-Path $gradleBuildDir 'libs'
if (-not (Test-Path -LiteralPath $libsDir)) {
    throw "Gradle completed, but '$libsDir' does not exist."
}

$allBuiltJars = @(Get-ChildItem -LiteralPath $libsDir -File -Filter '*.jar')
$runtimeJarCandidates = @(
    $allBuiltJars |
        Where-Object { -not (Test-IsExcludedJarName -Name $_.Name) } |
        ForEach-Object {
            $metadata = Get-JarModMetadata -Path $_.FullName
            [pscustomobject]@{
                File = $_
                Metadata = $metadata
                IsProjectMod = ($metadata.ModId -eq $modId)
                IsProjectVersion = ($metadata.Version -eq $newVersion)
            }
        } |
        Where-Object {
            ($_.IsProjectMod -and $_.IsProjectVersion) -or
            ($_.File.Name -eq "$modId-$newVersion.jar") -or
            ($_.File.Name -like "*$modId*" -and $_.File.Name -like "*$newVersion*")
        }
)

if ($runtimeJarCandidates.Count -ne 1) {
    $candidateNames = ($runtimeJarCandidates | ForEach-Object { $_.File.Name }) -join ', '
    $allNames = ($allBuiltJars | ForEach-Object { $_.Name }) -join ', '
    throw "Expected exactly one production runtime jar for '$modId' '$newVersion'. Candidates: [$candidateNames]. All jars: [$allNames]."
}

$sourceJar = $runtimeJarCandidates[0].File
$sourceMetadata = $runtimeJarCandidates[0].Metadata

$deletedOldJars = @()
$existingModJars = @(Get-ChildItem -LiteralPath $resolvedModsDir -File -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { Test-IsSameModJar -Jar $_ -ProjectModId $modId -ProjectModName $modName -JarNameMatch $configuredJarMatch })

foreach ($oldJar in $existingModJars) {
    $deletedOldJars += [ordered]@{
        Path = $oldJar.FullName
        SizeBytes = $oldJar.Length
        LastWriteTime = $oldJar.LastWriteTime.ToString('o')
    }
    Remove-Item -LiteralPath $oldJar.FullName -Force
}

$installedJarPath = Join-Path $resolvedModsDir $sourceJar.Name
Copy-Item -LiteralPath $sourceJar.FullName -Destination $installedJarPath -Force
$installedJar = Get-Item -LiteralPath $installedJarPath

$sourceHash = (Get-FileHash -LiteralPath $sourceJar.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
$installedHash = (Get-FileHash -LiteralPath $installedJar.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
$hashesMatch = $sourceHash -eq $installedHash
$sizesMatch = $sourceJar.Length -eq $installedJar.Length

$remainingMatchingJars = @(Get-ChildItem -LiteralPath $resolvedModsDir -File -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { Test-IsSameModJar -Jar $_ -ProjectModId $modId -ProjectModName $modName -JarNameMatch $configuredJarMatch } |
    ForEach-Object { $_.FullName })

$installedJarFullName = $installedJar.FullName
$onlyInstalledJarRemains = (
    $remainingMatchingJars.Count -eq 1 -and
    ([System.IO.Path]::GetFullPath($remainingMatchingJars[0]) -eq [System.IO.Path]::GetFullPath($installedJarFullName))
)

if (-not $hashesMatch) {
    throw 'Installed jar hash does not match source jar hash.'
}
if (-not $sizesMatch) {
    throw 'Installed jar size does not match source jar size.'
}
if (-not $onlyInstalledJarRemains) {
    throw "The mods directory still contains multiple or unexpected jars for '$modId'."
}

$versionDeclarationFiles = @(
    Get-VersionDeclarationStatus -Path $gradlePropertiesPath -Version $newVersion
    Get-VersionDeclarationStatus -Path $buildGradlePath -Version $newVersion
    Get-VersionDeclarationStatus -Path $buildGradleKtsPath -Version $newVersion
    Get-VersionDeclarationStatus -Path $neoforgeTomlPath -Version $newVersion
    Get-VersionDeclarationStatus -Path $modsTomlPath -Version $newVersion
    Get-VersionDeclarationStatus -Path $fabricJsonPath -Version $newVersion
)

$reportPath = Join-Path $gradleBuildDir 'install-report.json'
$report = [ordered]@{
    ModLoader = $modLoader
    ModId = $modId
    ModName = $modName
    PreviousVersion = $previousVersion
    InstalledVersion = $newVersion
    GradleCommand = ($gradleCommand -join ' ')
    SkipBuild = [bool]$SkipBuild
    NoVersionBump = [bool]$NoVersionBump
    GradleBuildDirectory = $gradleBuildDir
    ModsDirectory = $resolvedModsDir
    JarMatch = $configuredJarMatch
    SourceJar = $sourceJar.FullName
    InstalledJar = $installedJar.FullName
    SourceJarMetadata = $sourceMetadata
    SourceSizeBytes = $sourceJar.Length
    InstalledSizeBytes = $installedJar.Length
    SourceSHA256 = $sourceHash
    InstalledSHA256 = $installedHash
    HashesMatch = $hashesMatch
    SizesMatch = $sizesMatch
    DeletedOldJars = $deletedOldJars
    RemainingMatchingJars = $remainingMatchingJars
    OnlyInstalledJarRemains = $onlyInstalledJarRemains
    VersionDeclarations = $versionDeclarationFiles
    LiteralFilesUpdated = $literalUpdates
    GeneratedAt = (Get-Date).ToString('o')
}

$json = $report | ConvertTo-Json -Depth 8
Set-Content -LiteralPath $reportPath -Value $json -Encoding UTF8

Write-Host ''
Write-Host 'Minecraft mod install report'
Write-Host '----------------------------'
Write-Host "Loader: $modLoader"
Write-Host "Mod id: $modId"
Write-Host "Mod name: $modName"
Write-Host "Previous version: $previousVersion"
Write-Host "Installed version: $newVersion"
Write-Host "Source jar: $($sourceJar.FullName)"
Write-Host "Installed jar: $($installedJar.FullName)"
Write-Host "Source size: $($sourceJar.Length)"
Write-Host "Installed size: $($installedJar.Length)"
Write-Host "Source SHA-256: $sourceHash"
Write-Host "Installed SHA-256: $installedHash"
Write-Host "Hashes match: $hashesMatch"
Write-Host "Only installed jar remains: $onlyInstalledJarRemains"
Write-Host "Deleted old jars: $($deletedOldJars.Count)"
Write-Host "Report: $reportPath"
