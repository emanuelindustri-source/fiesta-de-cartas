# Contributing

Thanks for wanting to improve Fiesta de Cartas. This is an independent project
with a strong focus on Venezuelan card culture, original art and a polished
mobile experience.

## Before you start

- Read the [README](README.md) and the [online roadmap](docs/ONLINE_ROADMAP.md).
- For security issues, follow [SECURITY.md](SECURITY.md) instead of opening an issue.
- By contributing you agree to the contribution terms in [LICENSE](LICENSE).

## Development setup

Requirements: JDK 17, Android SDK 34, build-tools 34.0.0 and a configured
`ANDROID_HOME`.

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain
```

Install the debug APK (`com.fiestadecartas.app.debug`) on a device or emulator.

### Signing

Release builds read `keystore.properties` (ignored) or environment variables.
Never commit the keystore, its passwords or `local.properties`.

### Website

The static site lives in `site/` and has no build step. Open `site/index.html`
directly or serve it locally:

```powershell
python -m http.server 8080 --directory site
```

Then visit `http://localhost:8080`.

## Guidelines

- Keep the UI in the five supported languages (`values`, `values-en`,
  `values-pt`, `values-it`, `values-zh`). Every new user-facing string needs a
  key in all five files.
- Do not add trademarks, copied card art or assets without a compatible licence.
- Respect the existing package structure and Compose style.
- Keep game engines deterministic: LAN clients replay actions from the same seed.
- Never perform socket writes on the UI thread.
- Do not add real-money gambling, wallets, payouts or payment credentials.
  Virtual chips only; see `docs/ONLINE_ROADMAP.md`.
- Accessibility matters: content descriptions, touch targets of at least 48 dp,
  focus support for TV remotes and readable contrast.

## Pull requests

1. Create a branch: `feat/short-name`, `fix/short-name` or `docs/short-name`.
2. Make focused changes and test on at least one emulator.
3. Run `./gradlew :app:assembleDebug` and, when possible, a release build.
4. Describe what changed, why, and how you tested it. Include screenshots for UI
   changes.
5. Do not bump the version or publish releases; the maintainer handles that.
