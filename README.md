# Immersive Farming Mod Porting

This is an unofficial porting workspace for modernizing Etyl's Immersive Farming from Minecraft Forge 1.18.2 to Minecraft 1.21.1 on NeoForge and Java 21.

The fork preserves upstream attribution and MIT license text. It does not claim endorsement from Etyl, Immersive Engineering, BluSunrize, AstikorCarts, or AstikorCarts Redux authors.

## Port Scope

- NeoForge 1.21.1 project skeleton.
- Core farming systems: sprinklers, high-pressure sprinklers, irrigation, treated water disease cure, compost fertility, crop disease, farmland protection, bone meal controls, tilling changes.
- Industrial composter multiblock foundation.
- Integrated Immersive Cooking subsystem: grill oven, cookpot, food fermenter, food processor, juice fluids, recipes, assets, and JEI categories.
- Optional Farmer's Delight, Farm & Charm, and Vinery recipe compatibility data/providers.
- Optional AstikorCarts Redux compatibility data and adapter hooks instead of copying legacy cart internals.
- GameTest coverage for the critical farming rules.

## Validation

Run these locally before publishing any artifact:

```powershell
.\gradlew.bat build
.\gradlew.bat runGameTestServer
.\gradlew.bat runData
.\gradlew.bat scanAssets
.\gradlew.bat runServer
```

For Minecraft-loaded test builds, use the mandatory [install-mod.ps1](install-mod.ps1) workflow. It runs a clean build, installs the exact runtime jar into the target `mods` directory, and writes `build/install-report.json`.

Do not publish or upload artifacts automatically from this workspace.

## Porting Notes

See [docs/PORTING_FIXES.md](docs/PORTING_FIXES.md) for the Engineer's Manual localization fix and the NeoForge 1.21.1 multiblock repair notes.

See [MERGE_REPORT_IMMERSIVE_COOKING.md](MERGE_REPORT_IMMERSIVE_COOKING.md), [MERGE_INVENTORY_IMMERSIVE_COOKING.md](MERGE_INVENTORY_IMMERSIVE_COOKING.md), and [MIGRATION_IMMERSIVE_COOKING.md](MIGRATION_IMMERSIVE_COOKING.md) for the Immersive Cooking & Farming merge details.

Do not install a standalone Immersive Cooking jar alongside this merged jar. The merged jar preserves `immersivecooking:*` registry and resource IDs internally.
