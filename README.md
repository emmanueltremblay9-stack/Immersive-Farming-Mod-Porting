# Immersive Farming: Unofficial Modernization Fork

This is an unofficial porting workspace for modernizing Etyl's Immersive Farming from Minecraft Forge 1.18.2 to Minecraft 1.21.1 on NeoForge and Java 21.

The fork preserves upstream attribution and MIT license text. It does not claim endorsement from Etyl, Immersive Engineering, BluSunrize, AstikorCarts, or AstikorCarts Redux authors.

## Port Scope

- NeoForge 1.21.1 project skeleton.
- Core farming systems: sprinklers, high-pressure sprinklers, irrigation, treated water disease cure, compost fertility, crop disease, farmland protection, bone meal controls, tilling changes.
- Industrial composter multiblock foundation.
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

Do not publish or upload artifacts automatically from this workspace.
