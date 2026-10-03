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
- Targets Android and iOS, structured as a Kotlin Multiplatform project (`commonMain` /
  `androidMain` / `iosMain`). Building and running the iOS app requires a macOS host with Xcode
  installed (Kotlin/Native's iOS targets can only be compiled there).

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
    iosMain/
      kotlin/com/flagtutor/app/
        MainViewController.kt               Entry point called from Swift - hosts App composable
  build.gradle.kts
iosApp/                                    Xcode project - thin SwiftUI shell hosting the Compose UI
  iosApp.xcodeproj/
  iosApp/
    iOSApp.swift                           @main SwiftUI App entry point
    ContentView.swift                      Wraps MainViewController() in a UIViewControllerRepresentable
gradle/
  libs.versions.toml                       Centralized dependency version catalog
  wrapper/                                  Gradle wrapper
.claude/                                    Claude Code on the web config (Android SDK setup hook)
.github/workflows/                          CI: build APK + iOS simulator app on PRs, attach APK as a GitHub Release asset
```

## Architecture & conventions

The codebase is currently minimal (a single `App` composable). As features are added, follow the
same conventions used in the VideoDiary sibling project for consistency:

### Layers

- **`commonMain`**: Platform-agnostic Kotlin - domain models, view models, and Compose UI shared
  across platforms. Prefer putting new code here unless it needs a platform-specific API.
- **`androidMain`**: Android-specific code (e.g. `MainActivity`, Android resources, platform
  implementations of `expect`/`actual` declarations).
- **`iosMain`**: iOS-specific code (e.g. `MainViewController`, platform implementations of
  `expect`/`actual` declarations). The `iosApp/` Xcode project is a thin SwiftUI shell around it;
  new screens and business logic should still go in `commonMain`.

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

iOS can only be built on a macOS host with Xcode installed (Kotlin/Native has no cross-compiler
for Apple targets). On such a machine:

```bash
# Open the Xcode project directly - the "Compile Kotlin Framework" run script phase builds the
# composeApp KMP framework automatically as part of the Xcode build/run.
open iosApp/iosApp.xcodeproj

# Or build from the command line, e.g. for the simulator:
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' build
```

## CI/CD

- `.github/workflows/trigger_on_pull_request.yml` runs on PRs targeting `main`:
  1. Builds `assembleDebug`.
  2. Creates a draft GitHub Release tagged with the branch/run info and uploads the debug APK as an asset.
  3. Posts/updates a sticky PR comment with a direct download link to the APK.
  4. On a separate `macos-14` runner, builds the iOS app for the simulator via `xcodebuild`.

## General conventions for changes

- Follow the Page / PageContent / ViewModel split for new screens; keep `PageContent` composables
  free of ViewModel references so they remain previewable.
- Add new domain models to a `domain/model/` package, keeping them free of platform-specific types
  where possible.
- Add new dependencies to `gradle/libs.versions.toml` rather than hardcoding versions in
  `build.gradle.kts` files.
