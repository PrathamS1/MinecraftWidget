# MinecraftWidget

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Downloads](https://img.shields.io/github/downloads/PrathamS1/MinecraftWidget/total.svg?style=flat&color=3DDC84&logo=github&label=Downloads)](https://github.com/PrathamS1/MinecraftWidget/releases)
[![Discord](https://img.shields.io/badge/Discord-Join_Community-5865F2.svg?style=flat&logo=discord&logoColor=white)](https://discord.gg/ZHgSShkkx5)

MinecraftWidget is an open-source Android home screen widget application designed to bring animated Minecraft mobs directly to your launcher. Built with battery efficiency, low memory overhead, and an authentic Minecraft aesthetic at its core, the application runs smooth pixel-art animations using native system components without background battery drain.

---

## Current Status and Feature Overview

The project is fully functional, supporting custom-tailored widgets for Android home screens with complete offline autonomy.

### Key Capabilities
- **Local Asset Architecture**: All mob sprite sheets, frame timing definitions, and sound effects are bundled locally in the application package (`assets/mobs/`). The application performs zero network requests, requires no runtime downloads, and operates with 0.0 MB network cache.
- **Zero-Wake-Lock Battery Optimization**: Widgets run entirely on Android's native `RemoteViews` hierarchy backed by system `ViewFlipper`. When the device screen turns off or the home screen is not visible, the system automatically suspends rendering. No background daemon, background thread, polling loop, or CPU wake lock is kept alive.
- **Frame Rate Optimization and Pruning**: Sprite sheet animations are pruned and decimated to an authentic 10–14 FPS range. This preserves classic pixel-art motion fidelity while keeping transaction memory well within Android's strict Binder transaction buffer limits (`TransactionTooLargeException` prevention).
- **Interactive Tap Triggers**: Each placed widget can be configured with an independent on-tap action:
  - **Sound Effect**: Immediately triggers authentic mob cries, ambient noises, or step sounds via low-latency Android `SoundPool`.
  - **Play Animation**: Triggers the entity's complete sprite sheet action cycle.
  - **Physical Wobble**: Applies a spring-based squash-and-stretch wobble motion to the mob sprite.
- **Customizable Appearance**:
  - Background shapes: None (transparent cutout), Square, Rounded Rectangle, Circle, and Organic Blob.
  - Custom color palette and corner radius controls to complement various launcher themes and wallpapers.
  - Multiple widget size providers (Small 1x1 / 2x2, Medium, and Large layouts).
- **Minecraft Themed User Interface**: The companion application features a dedicated Minecraft-styled interface, including a custom options screen modeled after Minecraft settings menus, mob catalog filtering by category (Hostile, Passive, Neutral), and real-time live preview stages.

---

## Technical Architecture

MinecraftWidget is split into two communicating layers: the Jetpack Compose configuration UI and the native `AppWidgetProvider` rendering engine.

```
┌─────────────────────────────────────────────────────────────┐
│                       Companion App                         │
│  MainActivity ──> MainViewModel ──> CatalogRepository       │
│         │                                   │               │
│  Jetpack Compose Screens             assets/catalog.json    │
│  (MainScreen, Detail, Settings)      assets/sets.json       │
└──────────────────────────────┬──────────────────────────────┘
                               │ User pins/updates widget
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Widget Storage Layer                     │
│           WidgetConfigStore (Jetpack DataStore)             │
│            - Entity ID, SpriteSet ID, Background            │
│            - Shape, Color, Tap Action, Corner Radius        │
└──────────────────────────────┬──────────────────────────────┘
                               │ Trigger update
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Widget Runtime Engine                    │
│                                                             │
│  MobWidgetProvider (BroadcastReceiver)                      │
│    ├── WidgetUpdater (Batching & Throttling)                │
│    └── WidgetRenderer (Compose RemoteViews Hierarchy)       │
│          ├── SpriteSource (Read bundled WebP / PNG)         │
│          ├── DecodedSprite (Frame parsing & decimation)     │
│          ├── FramePlanner (Budget scaling & ViewFlipper)    │
│          └── CryPlayer (Low-latency SoundPool playback)     │
└─────────────────────────────────────────────────────────────┘
```

### Core Components and Logic

1. **`CatalogRepository` (`catalog/CatalogRepository.kt`)**
   - Parses `assets/catalog.json` and `assets/sets.json`.
   - Organizes mob metadata (names, categories, asset dimensions, sprite sheet layouts, and individual frame delays).
   - Operates as a thread-safe singleton managing the entity registry.

2. **`WidgetConfigStore` (`data/WidgetConfigStore.kt`)**
   - Backed by Android Jetpack DataStore Preferences.
   - Persists widget configurations indexed by `appWidgetId`: selected mob entity, selected sprite set, idle style, plate shape, background tint, corner radius, and tap behavior.
   - Cleans up configuration state when widgets are deleted from the launcher (`onDeleted`), and remaps IDs across device backup/restores (`onRestored`).

3. **`WidgetRenderer` (`widget/WidgetRenderer.kt`)**
   - The primary rendering pipeline that bridges assets and Android's remote process.
   - Resolves target widget dimensions via `AppWidgetManager.getAppWidgetOptions(widgetId)`.
   - Reads image files via `SpriteSource`, extracts frames via `BitmapOps`, applies procedural scaling, and composites background plates.
   - Generates an optimal `RemoteViews` layout. If the mob is animated, it feeds frames into a `ViewFlipper` with layout budget constraints calculated by `FramePlanner`.

4. **`FramePlanner` (`sprite/FramePlanner.kt`)**
   - Calculates the memory footprint of frames against the platform IPC transaction budget.
   - If a sprite sheet contains high frame counts or large dimensions, `FramePlanner` dynamically scales bitmaps or drops redundant frames to prevent out-of-memory errors on low-end hardware.

5. **`MobWidgetProvider` (`widget/MobWidgetProvider.kt`)**
   - Extends Android's `AppWidgetProvider`.
   - Handles `ACTION_APPWIDGET_UPDATE`, `ACTION_APPWIDGET_OPTIONS_CHANGED`, and custom tap broadcasts (`ACTION_TAP`).
   - Delegates audio events immediately to `CryPlayer` before background work begins, preventing audible delay on tap.
   - Uses `goAsync()` with a strict 8-second execution budget to prevent Application Not Responding (ANR) warnings.

6. **`CryPlayer` (`widget/CryPlayer.kt`)**
   - Manages audio resource loading and hardware sound playback.
   - Utilizes `SoundPool` for instantaneous local OGG asset decoding, ensuring taps produce instant feedback.

---

## Directory Structure

```
MinecraftWidget/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   ├── catalog.json          # Mob metadata and classification index
│   │   │   ├── sets.json             # Frame dimensions, delays, and sheet mappings
│   │   │   └── mobs/                 # Bundled entity sprite sheets and OGG sounds
│   │   ├── java/com/example/minecraftwidget/
│   │   │   ├── catalog/              # Entity catalog data models and repository
│   │   │   ├── data/                 # DataStore configurations and asset decoders
│   │   │   ├── sprite/               # Frame planning, bitmap operations, animation
│   │   │   ├── ui/                   # Jetpack Compose screens, theme, components
│   │   │   └── widget/               # RemoteViews renderer, updater, broadcast receivers
│   │   └── res/                      # Layouts, mipmaps, XML widget metadata
├── docs/                             # Additional mapping documentation
├── build.gradle.kts                  # Root build configuration
└── settings.gradle.kts               # Module settings
```

---

## Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer recommended.
- **JDK**: Java 17 or higher (compatible up to JDK 21+).
- **Android SDK**: Compile SDK 35, Minimum SDK 26 (Android 8.0 Oreo).

### Building and Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/<your-username>/MinecraftWidget.git
   cd MinecraftWidget
   ```

2. **Assemble the debug build:**

   - On Linux / macOS:
     ```bash
     ./gradlew assembleDebug
     ```
   - On Windows (PowerShell):
     ```powershell
     .\gradlew.bat assembleDebug
     ```

3. **Install on a connected device or emulator:**
   ```bash
   ./gradlew installDebug
   ```

4. **Add a widget to your home screen:**
   - Long-press on your device's home screen launcher and select **Widgets**.
   - Scroll to **MinecraftWidget** and drag a widget onto your home screen.
   - Alternatively, open the **MinecraftWidget** app, select any mob from the browse tab, and tap **Add to home screen**.

---

## Adding New Mobs

All entity definitions are data-driven. To add a new entity:

1. **Add Graphic Assets**:
   - Place sprite sheets or static textures inside `app/src/main/assets/mobs/<mob_id>/`.
   - Ensure the image has a transparent background (PNG or WebP format).

2. **Add Sound Effects**:
   - Place OGG audio files inside `app/src/main/assets/mobs/<mob_id>/sounds/`.

3. **Update Catalog Index**:
   - In `app/src/main/assets/catalog.json`, add an entry with the mob's ID, display name, category, and available variants.

4. **Update Frame Specifications**:
   - In `app/src/main/assets/sets.json`, add the sprite set configuration defining the frame width, frame height, total frame count, and frame delay list (e.g., `[80, 80, ...]`).

5. Rebuild the application using Gradle.

---

## Links & Community

- **Website**: https://prathams1.github.io/MinecraftWidget/
- **GitHub Releases**: https://github.com/PrathamS1/MinecraftWidget/releases
- **Discord Server**: https://discord.gg/ZHgSShkkx5

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed release notes and version history.

---

## Legal and Trademark Notice

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.

Minecraft is a trademark of Mojang Synergies AB. All mob models, textures, names, and sound effects are copyright and trademark property of Mojang Synergies AB and Microsoft Corporation. This project is an open-source fan application created strictly for personal and non-commercial educational use under fair use principles.

