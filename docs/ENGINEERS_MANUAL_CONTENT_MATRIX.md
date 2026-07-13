# Engineer's Manual Content Matrix

## Source of truth

- Current mod id: `immersive_farming_mod_porting`
- Merged cooking namespace: `immersivecooking`
- Manual integration: `src/main/resources/assets/immersiveengineering/manual/autoload.json`
- Manual category: `immersive_farming_mod_porting:farming_and_cooking` (`Farming and Cooking`)
- Immersive Engineering version inspected: `1.21.1-12.4.2-194`

## Farming and cooking category: farming entries

| Entry | Main source files | Covered in manual | Notes |
| --- | --- | --- | --- |
| `immersive_farming_mod_porting:overview` | `IFBlocks`, `IFItems`, `IFFluids`, `FarmingEvents` | Overall field loop, namespace, implemented content boundary | New entry |
| `immersive_farming_mod_porting:quick_start` | `FarmingEvents`, `SprinklerBlockEntity`, recipes | Basic player workflow from soil to irrigation | New entry |
| `immersive_farming_mod_porting:fertility` | `FertileSoilBlock`, `FarmingEvents`, `IFConfig` | Till, moisture, fertility, compost growth | Updated entry |
| `immersive_farming_mod_porting:irrigation` | `SprinklerBlock`, `SprinklerBlockEntity`, `FarmingLogic` | Normal and high pressure sprinkler coverage and consumption | Updated entry |
| `immersive_farming_mod_porting:diseases` | `FertileSoilBlock`, `DeadCropBlock`, `IFConfig` | Disease start, spread, lethality, treated-water recovery | Updated entry |
| `immersive_farming_mod_porting:composter` | `IndustrialComposterBlockEntity`, `ComposterRecipe`, composter recipes | Multiblock, tanks, energy, fluid loop, compost output | Updated entry |
| `immersive_farming_mod_porting:recipe_index` | `src/main/resources/data/immersive_farming_mod_porting/recipe` | Crafting recipes and custom composter recipe type | New entry |
| `immersive_farming_mod_porting:horsecarts` | `IntegrationPlaceholderItem`, cart-related recipes | Cart adapter items and optional integration boundary | Updated entry |
| `immersive_farming_mod_porting:configuration` | `IFConfig` | Runtime config values and behavior toggles | New entry |

## Farming and cooking category: cooking entries

| Entry | Main source files | Covered in manual | Notes |
| --- | --- | --- | --- |
| `immersivecooking:kitchen_overview` | `ICContent`, `MERGE_INVENTORY_IMMERSIVE_COOKING.md` | Preserved namespace and registered kitchen multiblocks | New entry |
| `immersivecooking:cookpot` | `CookpotLogic`, `DefaultCookpotRecipeProvider`, optional providers | Slots, energy, redstone, recipe adapters | Updated entry |
| `immersivecooking:food_fermenter` | `FoodFermenterLogic`, `DefaultFoodFermenterRecipeProvider`, compat recipes | Slots, tank, energy, redstone, Vinery/Farmer's Delight data | Updated entry |
| `immersivecooking:grill_oven` | `GrillOvenLogic`, `SmokingRecipeProvider` | Three-slot smoking, fuel behavior, no-FE rule | Updated entry |
| `immersivecooking:food_processor` | `FoodProcessorLogic`, `DefaultFoodProcessorRecipeProvider`, Food Processor recipe JSON | Registered structure/menu/recipe type, bundled default recipes, Farmer's Delight prep/cutting/pie/frozen/cold assembly compat recipes, and JEI category | New entry |
| `immersivecooking:compatibility` | Compat providers, conditional recipe JSON | Farmer's Delight, Farm & Charm, Vinery boundaries | New entry |

## Excluded or limited

| Feature | Reason |
| --- | --- |
| Standalone cart runtime behavior | Plow and sower are integration adapter items; actual cart behavior belongs to optional cart mods. |
| Exact JEI page layouts | JEI rendering is not part of the Engineer's Manual resource system. |
| In-game screenshot proof | Manual resources are validated by Gradle; visual inspection must be recorded in `ENGINEERS_MANUAL_VISUAL_QA.md` after opening the manual in a running client. |
