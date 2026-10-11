# MinecraftWidget Mapping

This document outlines how the concepts from Pokewidget will be adapted to create MinecraftWidget.

## 1. Sprites & Assets
- **Pokewidget:** Pokémon sprites (GIFs or static PNGs), cries (audio).
- **MinecraftWidget:** Minecraft Mob sprites (animated GIFs of walking/idle animations or static images), mob sounds (e.g., zombie groans, creeper hisses).
- **Variants:** Instead of "Shiny" Pokémon, we will have mob variants (e.g., Baby Zombie, Charged Creeper, Sheep colors).

## 2. Backgrounds & Scenery
- **Pokewidget:** Battle backgrounds (grass, water, cave).
- **MinecraftWidget:** Minecraft Biomes or specific blocks (Grass Block, Netherrack, End Stone, Sand) acting as the ground plate for the mob to stand on.

## 3. Interactions (Tap Actions)
- **Pokewidget:** Play Cry, Toggle Shiny, Flip, Mirror, Excite.
- **MinecraftWidget:**
  - **Play Sound:** Play the iconic mob sound.
  - **Toggle Variant:** Switch to a related variant (e.g., Normal to Charged Creeper).
  - **Flip:** Flip the sprite horizontally.
  - **Animate/Attack:** Trigger a specific animation (e.g., eating grass for sheep, attacking animation for zombies) by temporarily changing the spritesheet/GIF.

## 4. Technical Architecture
The core Android architecture remains the same:
- **AppWidgetProvider:** Handles widget lifecycle.
- **RemoteViews & ViewFlipper:** The exact same method will be used to animate the mob sprites without needing a foreground service.
- **FramePlanner:** Will still be necessary to ensure the GIF frames of Minecraft mobs don't exceed the `RemoteViews` IPC memory limits.
- **Config Store:** SharedPreferences/DataStore to save which mob is placed on which widget ID.
