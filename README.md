# MinecraftWidget 🟩⛏️

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Open Source](https://img.shields.io/badge/Open%20Source-%E2%99%A5-red.svg)](#)

**MinecraftWidget** is an open-source, battery-optimized Android home screen widget app that brings lively, animated Minecraft mobs directly to your launcher. Featuring authentic idle animations, mob cries and ambient sound effects, interactive tap actions, and customizable background plates.

---

## ✨ Features

- **Animated Minecraft Mobs**: Dozens of iconic mobs including Allay, Axolotl, Bee, Cat, Creeper, Enderman, Fox, Iron Golem, Pig, Polar Bear, Sheep, Skeleton, Sniffer, Warden, Wolf, Zombie, and more.
- **Zero-Wake-Lock Battery Efficiency**: Powered entirely by native Android `RemoteViews` and `ViewFlipper`. Never runs background polling, never keeps your CPU awake, and consumes 0% battery when your phone screen is off.
- **Pruned Frame Rates & Authentic Feel**: Animations are optimized with custom frame decimation (~10–14 FPS) to deliver classic pixel-art charm while keeping Android's widget transaction limits and memory consumption extremely light.
- **Interactive Taps**:
  - **🔊 Sound Effect**: Plays authentic entity sound effects (cries, grunts, chirps).
  - **✨ Animation Trigger**: Prompts the mob to play its active animation cycle.
  - **📳 Physical Wobble**: Playful spring-loaded wobble animation on tap.
- **100% Offline & Private**: All textures, sprite sheets, and OGG sound effects are bundled locally within the app APK. Zero internet permissions required, zero tracking, zero telemetry.
- **Customizable Appearance**:
  - Background shapes: `None`, `Square`, `Rounded`, `Circle`, `Blob`.
  - Palette color picker to match any wallpaper or theme.
  - Sizing and scaling options to fit 1x1, 2x2, or larger widget grids.
- **Modern Android Stack**: Built with Jetpack Compose, Material 3, and Kotlin Coroutines.

---

## 📱 Screenshots

> *Add your home screen widget previews and customization screen captures here!*

---

## 🛠️ Tech Stack & Architecture

- **UI & Configuration**: Jetpack Compose, Material 3, Activity-Compose.
- **Widget Engine**: Native `AppWidgetProvider`, `RemoteViews`, and `ViewFlipper` with layout-budget frame planning.
- **Storage**: Jetpack DataStore Preferences for persistence.
- **Audio**: Low-latency `SoundPool` for instantaneous local OGG playback.
- **Asset Pipeline**: Bundled lossless WebP/PNG sprite sheets organized by entity identifiers in `assets/mobs/`.

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 17 or higher
- **Android SDK**: Compile SDK 35, Minimum SDK 26 (Android 8.0 Oreo)

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/MinecraftWidget.git
   cd MinecraftWidget
   ```

2. Open the project in Android Studio or compile using the Gradle wrapper:

   **Linux / macOS:**
   ```bash
   ./gradlew assembleDebug
   ```

   **Windows (PowerShell):**
   ```powershell
   .\gradlew.bat assembleDebug
   ```

3. Install the debug APK to a connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```

---

## 🎨 Adding New Mobs

Adding a new mob is simple and requires no network endpoints:

1. **Sprite Sheets**: Place your idle/action sprite sheet (PNG or WebP) inside `app/src/main/assets/mobs/<mob_name>/`.
2. **Audio**: Place the authentic entity sound (OGG format) inside `app/src/main/assets/mobs/<mob_name>/sounds/`.
3. **Register Entity**:
   - Add the entity metadata to `app/src/main/assets/catalog.json`.
   - Add the frame layout, dimensions, and timing delay list to `app/src/main/assets/sets.json`.
4. Rebuild the app!

---

## 🤝 Contributing

Contributions are welcome! Whether you are adding mob sprites, improving animation timings, fixing bugs, or suggesting features:

1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/NewMobName`).
3. Commit your Changes (`git commit -m 'Add NewMobName sprites and sounds'`).
4. Push to the Branch (`git push origin feature/NewMobName`).
5. Open a Pull Request.

---

## 📜 Legal & Disclaimer

**NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.**

Minecraft is a trademark of Mojang Synergies AB. All mob models, textures, sound effects, and Minecraft names are copyright and trademark property of Mojang Synergies AB and Microsoft Corporation. This open-source project is created solely for personal and educational enjoyment by the Minecraft fan community under fair use.

---

## 📄 License

This project is open-source under the [MIT License](LICENSE).
