# Engineer's Manual Visual QA

## Status

Visual QA is pending until the client can be opened and the Engineer's Manual pages can be inspected manually. The Gradle validator verifies resource presence and element references, but it cannot prove page layout, text wrapping, or icon rendering.

## Checklist

- Open Minecraft 1.21.1 with NeoForge 21.1.233 and Immersive Engineering 12.4.2-194.
- Open the Engineer's Manual.
- Confirm the `Farming and Cooking` category appears.
- Open every farming entry in `Farming and Cooking` and verify:
  - title and subtitle display;
  - crafting, item display, and multiblock elements render;
  - English text wraps without clipping.
- Switch language to French.
- Reopen every farming entry and verify French text renders.
- Open every cooking multiblock entry in `Farming and Cooking` and verify:
  - Cookpot, Food Fermenter, Grill Oven, and Food Processor multiblock previews render;
  - compatibility and overview item displays render;
  - Food Processor default recipe examples render;
  - Food Processor text names Farmer's Delight prep, cutting, pie, frozen, and cold assembly recipes as conditional compat content.
- Confirm no `NoSuchElementException` or missing manual resource error is logged.

## Last recorded result

- Not yet visually executed in this repository state.
