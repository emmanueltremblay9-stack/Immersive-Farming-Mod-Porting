# Immersive Cooking Merge Report

## Result

Immersive Cooking & Farming has been imported as a subsystem of Immersive Farming Mod Porting. The merge preserves the upstream `immersivecooking` namespace and avoids registering a second mod entry.

## Branch and Source

- Working branch: `codex/immersive-cooking-full-merge`
- Target repository head before this merge work: `5f9f331 Fix IE manual autoload resources`
- Upstream source commit: `76e033a712f8c1a68e40e6630801c0345dbe467b`
- Upstream source branch: `1.21.1`

## Architecture

- Target entrypoint `dev.emmanueltremblay.immersivefarming.ImmersiveFarming` initializes `ImmersiveCooking.init(modEventBus)` only when the standalone `immersivecooking` mod is not loaded.
- Imported code is relocated under `com.oblixorprime.immersivefarming.immersivecooking` to avoid JPMS package exports colliding with the standalone Immersive Cooking mod.
- Imported resources remain under `assets/immersivecooking` and `data/immersivecooking`.
- No standalone Immersive Cooking mod jar is required at runtime.

## Main Migration Work

- NeoForge registry migration from older Forge patterns to `DeferredHolder` and the existing target mod event bus.
- Immersive Engineering 1.21.1 multiblock process and recipe holder migration.
- NeoForge capability migration for item, energy, and fluid access.
- NeoForge fluid registration and bucket behavior migration.
- JEI NeoForge API migration, including fluid ingredient types and vanilla recipe holder transfer types.
- Optional Farmer's Delight and Farm & Charm compile dependencies switched to NeoForge files.
- Imported recipe JSONs normalized for NeoForge condition and item stack codecs.

## Validation Status

- `powershell -ExecutionPolicy Bypass -File .\install-mod.ps1 -NoVersionBump` passed on 2026-07-13 and ran `.\gradlew.bat clean build` for version `1.21.1-0.1.77`.
- `.\gradlew.bat runGameTestServer --no-daemon --console plain` passed all 58 required tests on 2026-07-13.
- The runtime JAR was installed into the NeoForge 1.21.1 Prism LAB instance. Source and installed SHA-256 were both `8c4945d6478124cc9f94ff1b0b3bbbb0c7921c2eb2e28a88bc18dbf1d4812c6b`, and exactly one matching JAR remained.
- Data generation and live client/visual gameplay validation were not performed in this publication pass.

## Known Risks

- Imported fluid recipe tags still include some `forge:*` tag names to preserve upstream compatibility data. NeoForge common tags may require an additional `c:*` migration if recipes do not match in a live pack.
- Optional compat providers compile against current NeoForge variants of Farmer's Delight and Farm & Charm, but live behavior depends on those mods' runtime APIs staying compatible.
- The workspace already contained unrelated dirty files before this merge; this report covers only the Immersive Cooking integration work.
