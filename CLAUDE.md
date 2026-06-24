# CLAUDE.md

This file provides guidance for AI assistants (and developers) working with this repository.

# Commands
Here is a list of short-hand commands that you may be asked.
- "Implement the top todo" - read the TODO.md file and inplement the top item, following the rules described in that file.
- "Delete pre-releases" - run the "delete_prereleases" workflow yml

## Project overview

**FlagTutor** is an Android game (Kotlin Multiplatform + Compose Multiplatform) that helps users
learn the flags of the world.

- Package: `com.flagtutor.app`
- More context: [README.md](README.md)

## Tech stack

- **Language**: Kotlin
- **UI**: Compose Multiplatform (Material 3)
- **Build**: Gradle Kotlin DSL, dependency versions centralized in `gradle/libs.versions.toml` (version catalog)
- **Min/Target/Compile SDK**: minSdk 24, targetSdk/compileSdk 35
- **Versions**: Kotlin 2.1.0, AGP 8.7.3, Compose Multiplatform 1.7.3, Java 11 target
- Currently targets Android only, but is structured as a Kotlin Multiplatform project
  (`commonMain` / `androidMain`) so other platforms can be added later.

## Repository layout

```
composeApp/                                Kotlin Multiplatform module
  src/
    commonMain/kotlin/com/flagtutor/app/
      App.kt                                Root Composable
    androidMain/
      kotlin/com/flagtutor/app/
        MainActivity.kt                     Single Activity - hosts App composable
      res/                                  Android resources (icons, strings, themes)
      AndroidManifest.xml
  build.gradle.kts
gradle/
  libs.versions.toml                       Centralized dependency version catalog
  wrapper/                                  Gradle wrapper
.claude/                                    Claude Code on the web config (Android SDK setup hook)
.github/workflows/                          CI: build APK on PRs, attach as a GitHub Release asset
```

## Architecture & conventions

The codebase is currently minimal (a single `App` composable). As features are added, follow the
same conventions used in the VideoDiary sibling project for consistency:

### Layers

- **`commonMain`**: Platform-agnostic Kotlin - domain models, view models, and Compose UI shared
  across platforms. Prefer putting new code here unless it needs a platform-specific API.
- **`androidMain`**: Android-specific code (e.g. `MainActivity`, Android resources, platform
  implementations of `expect`/`actual` declarations).

### Feature structure (`commonMain/kotlin/com/flagtutor/app/ui/feature/<name>/`)

As screens are added, give each one:
- `<Name>Page.kt` — thin composable that wires a ViewModel's state/callbacks into `<Name>PageContent`
- `<Name>PageContent.kt` — "dumb" composable taking explicit parameters (state + lambdas), easy to preview
- `<Name>ViewModel.kt` — exposes Compose state via `mutableStateOf`/`derivedStateOf`, not `StateFlow`/`LiveData`
- `component/` — subcomponents specific to that feature

### ViewModels

- Expose state as Compose `mutableStateOf`/`mutableIntStateOf`/`derivedStateOf` properties with
  `private set`.
- Run async work in `viewModelScope.launch { ... }`.

### Dependency Injection

- Use Koin (`koin-compose`, `koin-compose-viewmodel`) for DI, with module definitions grouped in a
  `di/KoinModule.kt`, following the VideoDiary pattern (`factory { }` for most things, `single { }`
  for stateful singletons).

### Logging

- Use a `Logger` abstraction (mirroring `domain/util/logger/Logger.kt` in VideoDiary) rather than
  calling platform logging APIs directly, so logging is consistent across platforms and doesn't
  break tests.
  - `Logger.debug(...)` — diagnostics
  - `Logger.warning(...)` — expected-but-notable error states
  - `Logger.error(...)` — unexpected errors / likely bugs

## Build, run & test

```bash
# Build debug APK
./gradlew assembleDebug

# Run JVM unit tests
./gradlew test

# Full check (build + tests + lint)
./gradlew check
```

## CI/CD

- `.github/workflows/trigger_on_pull_request.yml` runs on PRs targeting `main`:
  1. Builds `assembleDebug`.
  2. Creates a draft GitHub Release tagged with the branch/run info and uploads the debug APK as an asset.
  3. Posts/updates a sticky PR comment with a direct download link to the APK.

## General conventions for changes

- Follow the Page / PageContent / ViewModel split for new screens; keep `PageContent` composables
  free of ViewModel references so they remain previewable.
- Add new domain models to a `domain/model/` package, keeping them free of platform-specific types
  where possible.
- Add new dependencies to `gradle/libs.versions.toml` rather than hardcoding versions in
  `build.gradle.kts` files.
