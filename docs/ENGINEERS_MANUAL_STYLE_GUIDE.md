# Engineer's Manual Style Guide

## Scope rules

- Document implemented behavior from current Java, JSON, and packaged resources.
- Mark placeholders, optional integrations, and missing recipes explicitly.
- Use `immersive_farming_mod_porting` for farming entries and `immersivecooking` for merged cooking entries.
- Prefer Immersive Engineering manual elements already supported by IE 12.4.2: `crafting`, `item_display`, and `multiblock`.

## Writing rules

- First line is the entry title.
- Second line is the short subtitle.
- Use short paragraphs and page breaks with `<np>` only where the entry naturally changes topic.
- Every `<&element>` reference must have a matching key in the entry JSON.
- Keep English and French entries structurally aligned so future edits can be reviewed side by side.

## Localization rules

- Every autoloaded entry needs:
  - `assets/<namespace>/manual/<entry>.json`
  - `assets/<namespace>/manual/en_us/<entry>.txt`
  - `assets/<namespace>/manual/fr_fr/<entry>.txt`
- Every autoloaded category and entry should have language keys in the matching namespace language files.
- French text may use accents. Resource file names and manual entry IDs stay lowercase ASCII.

## Content quality rules

- Do not call a recipe or machine complete unless the current jar ships the recipes and runtime logic.
- Do not document optional-mod behavior as available unless the optional mod is loaded or the adapter is conditional.
- If a feature exists as data-pack extension surface only, say so directly.
