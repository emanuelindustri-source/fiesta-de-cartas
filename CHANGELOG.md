# Changelog

All notable changes to Fiesta de Cartas are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
semantic versioning.

## [1.1.0] — 2026-10-21

### Added

- Player profile with experience, levels 1–100 and a long-term progression curve.
- Ten original avatars inspired by Venezuelan wildlife, selectable in the profile.
- Configurable match rules: players and target score for Caída and Fiesta de
  Colores, tokens to win for Ludo, starting cards for Nervioso.
- In-app rating screen with stars, categories and a comment sent through the
  device email app to `emanuel.industri@gmail.com`.
- Device detection for phone, tablet, TV and dual-screen layouts, including a
  Leanback launcher entry and D-pad focus.
- Animated Caída table: staggered dealing, card flight, swipe-to-play, shake on
  capture, avatar reactions, sounds and haptics.
- Animated Ludo die with bounce, sound and vibration.
- Virtual practice chips and a clear no-real-money notice.
- Static website in `site/` with privacy policy, terms, SEO, Open Graph assets and
  GitHub Pages deployment.
- CI for debug builds, signed releases and dependency updates.
- Security, contribution, licence and roadmap documentation.

### Changed

- Network writes moved to single-thread executors, fixing
  `NetworkOnMainThreadException` in LAN matches.
- Player badges now show avatars and reactions in all games.
- Caída points target is no longer hardcoded to 24.
- Release signing credentials moved out of `build.gradle.kts` into an ignored
  `keystore.properties` or CI secrets.
- Version bumped to 1.1.0 (versionCode 2).

### Fixed

- Guest-to-host and host-to-guest actions now arrive reliably during LAN matches.
- Cards played during a round are revealed after the flight animation.
- Stale "¡CAÍDA!" banners no longer remain on screen after the next play.

## [1.0.0] — 2026-10-20

- First signed release: Caída, Ludo, Fiesta de Colores, Kemix and Nervioso.
- Five languages, seasonal themes, local WiFi multiplayer, QR rooms and APK
  sharing.
- AdMob integration with test IDs and consent.
