# Changelog

All notable changes to the **MinecraftWidget** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0] - 2026-10-12

### Links
- **Website**: https://prathams1.github.io/MinecraftWidget/
- **GitHub Releases**: https://github.com/PrathamS1/MinecraftWidget/releases/tag/v1.0
- **Discord Community**: https://discord.gg/ZHgSShkkx5

### Added
- **Initial Official Release** of MinecraftWidget for Android 8.0+ (API 26+).
- **Native Android Home Screen Widgets**:
  - Full support for Android AppWidget framework with zero background battery drain via system-managed `RemoteViews` and `ViewFlipper`.
  - Multiple widget size providers (Small 1x1 / 2x2, Medium, and Large layouts).
- **Interactive Tap Actions**:
  - Sound effect playback powered by low-latency Android `SoundPool`.
  - Play sprite animation cycles on tap.
  - Spring-based physical squash-and-stretch wobble motion.
- **Deep Widget Customization**:
  - Background cutout shapes: Transparent (None), Square, Rounded Rectangle, Circle, and Organic Blob.
  - Custom color palettes and adjustable corner radiuses.
  - Horizontal mirroring (facing left or right).
  - Custom name tags (Minecraft oak sign or raw pixel text) with customizable text.
- **In-App Widget Management**:
  - Authentic Minecraft Hotbar at the bottom of the app displaying currently placed home screen widgets.
  - Dedicated "Edit Widget" screen allowing direct reconfiguration of any placed widget with live previews.
- **Companion App & Theming**:
  - Authentic Minecraft HUD aesthetic with pixel typography (`Geist Pixel` & `MinecraftSeven`), stone textures, and dirt backgrounds.
  - Categorized mob browsing (Hostile, Passive, Neutral) with real-time search.
  - Dedicated Minecraft options and settings screen.
- **Fully Offline Architecture**:
  - All sprite sheets, static textures, frame definitions, and audio files bundled locally in the APK package.
  - Zero external network requests, zero analytics tracking, and zero runtime downloads.
