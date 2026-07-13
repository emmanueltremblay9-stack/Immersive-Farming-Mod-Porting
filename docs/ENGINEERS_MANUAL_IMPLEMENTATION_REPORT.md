# Engineer's Manual Implementation Report

## Summary

The Engineer's Manual has been expanded to cover the current implemented Immersive Farming Reforged and merged Immersive Cooking behavior in English and French.

## Manual integration

- Updated `assets/immersiveengineering/manual/autoload.json` to include farming overview, quick start, recipe index, configuration, cooking overview, food processor, and compatibility entries.
- Merged the previous `immersive_farming_mod_porting:farming` and `immersivecooking:multiblocks` categories into `immersive_farming_mod_porting:farming_and_cooking`, displayed as `Farming and Cooking`.
- Kept the old data-path autoload mirror in sync for repository validation compatibility.

## Validator changes

- `scanManualReferences` now requires both `en_us` and `fr_fr` text files for every autoloaded entry.
- `scanManualReferences` still verifies every `<&element>` in manual text is defined in the matching manual JSON.
- Added `validateEngineersManual`, depending on manual reference validation and imported-class packaging validation.
- `check` depends on `validateEngineersManual`.

## Known limitations

- Cart automation is documented as optional integration because the plow and sower are adapter items, not standalone machines.
- Visual layout inspection still requires opening the Engineer's Manual in a running client.
