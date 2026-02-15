# Gemini Code Companion

## Project Overview

This project is **HexaMusicPlayer**, a native Android music player.

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** Hexagonal (domain/use case/port + infrastructure adapters)
- **Media Engine:** Media3 ExoPlayer
- **Background Playback:** Media3 `MediaSessionService` (`PlaybackMediaSessionService`)

## Build and Run

- Build quality + artifacts:

```bash
./gradlew lint test assembleDevDebug assembleProdRelease
```

- Install dev debug:

```bash
./gradlew installDevDebug
```

## Notes

- Release signing is configured via secrets/env vars (`RELEASE_STORE_*`, `RELEASE_KEY_*`).
- CI workflow is defined in `.github/workflows/android-ci.yml`.
- Main documentation is in `README.md` and `docs/RELEASE.md`.
