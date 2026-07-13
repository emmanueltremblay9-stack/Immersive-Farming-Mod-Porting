# Immersive Cooking Migration Notes

## Registry ID Policy

Existing upstream `immersivecooking:*` IDs are preserved. They are packaged inside the `immersive_farming_mod_porting` jar and initialized by that mod, but worlds and data packs should continue to refer to `immersivecooking:*` resources.

## User Migration

No separate Immersive Cooking jar should be installed next to this merged jar. Installing both can duplicate registry IDs and recipes.

If a previous test instance has an old standalone Immersive Cooking jar, remove it before launching with the merged Immersive Farming jar.

## Data Pack Migration

- Recipe types remain:
  - `immersivecooking:cookpot`
  - `immersivecooking:food_fermenter`
  - `immersivecooking:food_processor`
- Recipe condition type was migrated to `neoforge:mod_loaded`.
- Custom recipe top-level item stack fields should use `id`, for example:

```json
{
  "result": {
    "id": "vinery:apple_wine"
  }
}
```

Ingredient entries may still use standard recipe ingredient syntax such as `"item"` or `"tag"`.

## Runtime Dependency Migration

- Required for the base project: NeoForge 1.21.1 and Minecraft 1.21.1.
- Immersive Engineering remains optional in metadata but is required for the imported multiblock functionality to be useful.
- JEI, Farmer's Delight, Farm & Charm, and Vinery are optional integrations.

## Removed Runtime Surface

- Upstream standalone `@Mod` registration was removed.
- Upstream mixin configuration was removed from the merged jar.
- Upstream datagen-only recipe builder classes were not retained as runtime classes.
