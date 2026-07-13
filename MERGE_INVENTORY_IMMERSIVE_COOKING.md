# Immersive Cooking Merge Inventory

## Source

- Upstream repository: `https://github.com/akkiserver-dev/Immersive-Cooking`
- Branch: `1.21.1`
- Imported commit: `76e033a712f8c1a68e40e6630801c0345dbe467b`
- Commit date: `2026-05-05T13:29:31+09:00`
- Commit subject: `wip`

## Import Counts

- Java files under `src/main/java/com/oblixorprime/immersivefarming/immersivecooking`: 72
- Asset files under `src/main/resources/assets/immersivecooking`: 63
- Data files under `src/main/resources/data/immersivecooking`: 94
- Recipe JSON files under `src/main/resources/data/immersivecooking/recipes`: 72

## Namespace Strategy

The merge preserves upstream registry and resource IDs in the `immersivecooking` namespace. The Java code is relocated under the target mod namespace to avoid JPMS exported-package collisions with the standalone Immersive Cooking mod. The code is initialized by the target `immersive_farming_mod_porting` mod entrypoint when the standalone `immersivecooking` mod is absent, but no second `@Mod` entry is registered for Immersive Cooking.

This keeps existing upstream worlds, data packs, recipes, and resource references closer to their source IDs while packaging them inside the merged target jar.

## Main Registry IDs Preserved

### Multiblocks

- `immersivecooking:grill_oven`
- `immersivecooking:cookpot`
- `immersivecooking:food_fermenter`
- `immersivecooking:food_processor`

### Menus

- `immersivecooking:grill_oven`
- `immersivecooking:cookpot`
- `immersivecooking:food_fermenter`
- `immersivecooking:food_processor`

### Recipes and Serializers

- `immersivecooking:cookpot`
- `immersivecooking:food_fermenter`
- `immersivecooking:food_processor`

### Fluids

- `immersivecooking:apple_juice`
- `immersivecooking:red_grapejuice`
- `immersivecooking:red_taiga_grapejuice`
- `immersivecooking:red_jungle_grapejuice`
- `immersivecooking:red_savanna_grapejuice`
- `immersivecooking:white_grapejuice`
- `immersivecooking:white_taiga_grapejuice`
- `immersivecooking:white_jungle_grapejuice`
- `immersivecooking:white_savanna_grapejuice`

Each fluid also registers a flowing fluid, fluid block, and bucket item using the upstream naming pattern.

### Other Content

- Creative tab: `immersivecooking:main`
- Sounds:
  - `immersivecooking:block.cookpot.active`
  - `immersivecooking:block.food_fermenter.active`

## Migration Status

- Imported source compiles under NeoForge 1.21.1 after API migration.
- Recipe conditions were migrated from `forge:mod_loaded` to `neoforge:mod_loaded`.
- Top-level item stack objects in custom recipe `result` and `container` fields were migrated from `item` to `id`.
- Optional Farmer's Delight and Farm & Charm provider construction is gated on the optional mod being loaded.
- Upstream mixin and datagen-only recipe builder classes were removed from the merged runtime surface.
