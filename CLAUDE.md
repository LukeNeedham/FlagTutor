# CLAUDE.md

This file provides guidance for AI assistants (and developers) working with this repository.

# Commands
Here is a list of short-hand commands that you may be asked.
- "Implement the top todo" - read the TODO.md file and inplement the top item, following the rules described in that file.
- "Delete pre-releases" - run the "delete_prereleases" workflow yml

## Project overview

**Vexed** is an Android game (Kotlin Multiplatform + Compose Multiplatform) that helps users
learn the flags of the world.

- Package: `com.flagtutor.app`
- More context: [README.md](README.md)

## Tech stack

- **Language**: Kotlin
- **UI**: Compose Multiplatform (Material 3)
- **Build**: Gradle Kotlin DSL, dependency versions centralized in `gradle/libs.versions.toml` (version catalog)
- **Min/Target/Compile SDK**: minSdk 24, targetSdk/compileSdk 35
- **Versions**: Kotlin 2.2.21, AGP 8.13.2, Compose Multiplatform 1.10.3, Java 11 target
- Targets Android, iOS and web (Kotlin/Wasm), structured as a Kotlin Multiplatform project (`commonMain` /
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

### Web target

- `wasmJsMain` holds the browser entry point (`Main.kt`), `webModule()` and the web `actual`s.
- Room has no web support, so the persistence code (`data/stats` entity/DAO/database) lives in
  `roomMain`, an intermediate source set that `androidMain` and `iosMain` depend on (web does not). Web uses
  `LocalStorageFlagAttemptRepository` instead. Anything using Room must go in `roomMain`.
- Browser URLs follow in-app navigation (`/`, `/play`, `/credits`; see `BrowserRoutes`,
  `BrowserNavigationSync`), so the back button works. GitHub Pages can't serve those paths, so
  `.github/pages/404.html` (published to the `gh-pages` root by the preview workflow) redirects them
  to `index.html`, which restores the URL. New routable screens need an entry in `BrowserRoutes` (debug pages are at `/debug/...`, debug builds only).
- `isDebugBuild` on web is read from `config.js` (`window.appDebug`), which is `false` in the
  repo. PR previews overwrite it with `true`; the production deploy
  (`.github/workflows/web_deploy_production.yml`, on every push to `main`, to the `gh-pages` root)
  leaves it `false`. Previews live under `/pr-<n>/` and survive production deploys.
- Game images are loaded ahead of time by `FlagImageRepository` (current + next country), so the
  game screen never shows a spinner between flags.
- `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` serves it locally;
  `./gradlew :composeApp:wasmJsBrowserDistribution` produces the static site in
  `composeApp/build/dist/wasmJs/productionExecutable`.

### Colours

- Never hardcode colours (`Color.White`, `Color(0xFF...)`, hex in composables) in UI code. Always read
  them from the theme: `AppTheme.colors.*` (`ui/theme/AppColors.kt`), named by usage. Likewise use
  `AppTheme.typography.*` and `AppTheme.shapes.*`. Never reference `MaterialTheme` in app code; it is only
  populated from `AppTheme` in `Theme.kt` so Material components pick up matching defaults. Define raw
  values only in `ui/theme/Color.kt`, with a light and a dark variant, and check contrast in both themes.
- Exceptions: colours that are data rather than styling (per-flag colours from `FlagColorDataSource`)
  and the Android launcher icon resources.

### Animations

- Never hardcode an animation duration (`tween(300)`, `delay(60)`, ...). Take it from `LocalScaledAnimation.current`
  (`ui/util/ScaledAnimation.kt`), which defines the allowed durations with the debug animation speed setting
  already applied. To add a new duration, add a property there.

### Preferences

- On Android, always use Jetpack DataStore (`datastore-preferences`) for key/value preferences, never
  `SharedPreferences`. See `DataStoreThemePreferenceStore` for the pattern.

### Flags

- Never round the corners of (or clip to a rounded shape) the flag image: not all flags are rectangular.

### Flag colours

- Each flag's dominant colours are extracted at build time by `buildSrc`'s
  `GenerateFlagColorsTask` (`./gradlew :composeApp:generateFlagColors`) into
  `files/flag_colors.json`, registered as a compose resource directory. At runtime
  `FlagColorDataSource` just reads that file, so there is no per-platform image analysis.

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
  1. Calls the shared `android_pr_build.yml` from `LukeNeedham/ci-workflows` (`@main`), which builds
     `assembleDebug`, creates a pre-release tagged with the branch/run info with the debug APK as an
     asset, and posts/updates a sticky PR comment with a direct download link to the APK.
  2. The `macos-14` iOS simulator build (`build-ios`) is skipped on PRs; it only runs when the
     workflow is dispatched manually.
- `.github/workflows/on_pull_request_closed.yml` calls the shared `android_pr_cleanup.yml`, which
  deletes the PR's build pre-releases (and tags) when the PR merges.
- The APK build/cleanup logic lives in the shared repo (`LukeNeedham/ci-workflows`), not here, so change it there.

## General conventions for changes

- Follow the Page / PageContent / ViewModel split for new screens; keep `PageContent` composables
  free of ViewModel references so they remain previewable.
- Add new domain models to a `domain/model/` package, keeping them free of platform-specific types
  where possible.
- Add new dependencies to `gradle/libs.versions.toml` rather than hardcoding versions in
  `build.gradle.kts` files.
- `.github/workflows/web_preview.yml` builds the web app on each PR and deploys it to GitHub Pages
  at `https://<owner>.github.io/<repo>/pr-<number>/` (via the `gh-pages` branch), with a sticky PR
  comment linking to it. The preview is removed when the PR closes. One-time setup: in repo
  Settings → Pages, set the source to "Deploy from a branch" → `gh-pages` / root.
