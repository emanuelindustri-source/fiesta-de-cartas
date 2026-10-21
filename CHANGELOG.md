# Changelog

All notable changes to Fiesta de Cartas are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
semantic versioning.

## [1.3.1] — 2026-10-21

### Fixed

- Seasonal background no longer recomposes the whole screen every animation frame;
  only the decorative layer updates, improving scrolling and battery use.
- The background animation runs at a calmer 22 fps, which is enough for fog and
  particles without competing with card animations.

## [1.3.0] — 2026-10-21

### Added

- In-match history in all five games: the last five actions are always visible and a
  submenu opens the complete match log with copy-to-clipboard.
- Bot difficulty selector (Relaxed, Balanced, Challenging) persisted across matches,
  with distinct decision-making in every engine.
- New adaptive app icon and in-app brand mark with a card fan and seasonal badge.
- Richer 44.1 kHz stereo seasonal music and sound effects encoded as OGG, with
  multi-tap ambience and clearer transients.
- Card flight animation in Fiesta de Colores and 3D flip animation in Kemix.

### Changed

- Ludo board now has a subtle perspective tilt, a more dimensional frame, token
  shadows/highlights and a marker for the last moved token.
- Seasonal scenes and particles are lighter on the CPU.
- Default bot level is Balanced; Relaxed makes mistakes and Challenging punishes
  unsafe plays.

## [1.2.0] — 2026-10-21

### Added

- Seasonal art overhaul: Halloween is active for the whole of October, with a moon,
  graveyard silhouettes, bare trees, cobwebs, bats, drifting embers and animated fog.
  Christmas, Reyes, Valentine, Easter and Fiesta have their own particles and scenes.
- Procedural seasonal soundscapes and sound effects: ambient loops, card deal, slam,
  canto, dice, win, coin and spooky whoosh, respecting the sound setting.
- Completely redrawn Spanish deck: aged paper, filigree corners, real pip layouts,
  illustrated Rey, Caballo and Sota, plus a richer card back.
- Glossier colour cards, a 3D die, a detailed Ludo board with shadows, stars,
  carved frame and dimensional tokens, and a gold coin component.
- Hot-seat mode (no bots) for all five games, in addition to bots and WiFi.
- WiFi rooms for Kemix and Nervioso, with host-authoritative actions.
- Honeygain support bridge: consent-first, foreground-only, disabled by default.
  The proprietary AAR/API key are intentionally not committed.
- AdMob publisher id and `app-ads.txt` prepared for account verification.

### Changed

- Home match chooser redesigned with fixed action buttons and rules scroll.
- Header and screen titles show the active festival mark.
- Theme now covers menus, submenus and game screens, not only the home hero.

### Fixed

- LAN socket writes remain off the UI thread.
- Local multiplayer uses the active seat for hands, turns and slaps.

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
