# AGENTS.md

## Project type

This repository is a Minecraft mod project.

The agent must treat build and install verification as mandatory. The main recurring failure to prevent is: building a new jar but not installing the exact jar that Minecraft loads.

## Mandatory Minecraft mod install workflow

For any task that changes Java code, Kotlin code, resources, assets, data packs, recipes, registries, mixins, metadata, Gradle configuration, or anything that affects runtime behavior:

1. Detect the current mod id.
2. Detect the current version.
3. Bump the version for every user-visible test build.
4. Synchronize the version in every relevant metadata/config file.
5. Run a clean Gradle build.
6. Select the correct runtime jar from `build/libs`.
7. Remove previous installed jars for this same mod from the configured Minecraft `mods` directory.
8. Copy the newly built runtime jar into the configured Minecraft `mods` directory.
9. Verify SHA-256 equality between the built jar and installed jar.
10. Verify exactly one jar for this mod remains in the target `mods` directory.
11. Produce an install report.

The task is not complete if any of these checks fail.

## Versioning rule

If the version is formatted like:

```text
0.0.0.1
```

increment the last numeric component:

```text
0.0.0.2
```

For versions with a Minecraft/API prefix, also increment the last numeric component while preserving the prefix:

```text
1.21.1-0.1.2 -> 1.21.1-0.1.3
```

Do not reuse the same version twice for user-visible test builds.

## Installed test target

Default local test `mods` directory:

```text
C:\Users\Emmanuel Tremblay\OneDrive\Documents\Immersive Farming Mod Porting\runs\client\mods
```

Use the repository script for installation unless the user explicitly targets a different Minecraft instance:

```powershell
powershell -ExecutionPolicy Bypass -File .\install-mod.ps1
```

The script must perform the whole sequence automatically: bump version, clean build, select the runtime jar, delete older jars for the same mod, copy the new jar, verify SHA-256, verify only one matching jar remains, and write an install report.
