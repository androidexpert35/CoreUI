# CoreUI

[![Version](https://img.shields.io/badge/version-1.0.5-blue.svg)](https://github.com/androidexpert35/CoreUI/packages)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg)](https://android-arsenal.com/api?level=29)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF.svg)](https://kotlinlang.org)
[![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.03.01-4285F4.svg)](https://developer.android.com/jetpack/compose/bom)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Build features, not presentation plumbing

Every Android feature needs the same foundations: loading and error states, retry behavior,
one-shot effects, navigation events, and a predictable contract between the `ViewModel` and
Compose.

**CoreUI packages those foundations into one small, reusable Jetpack Compose library.** It gives
your screens a consistent state model, lifecycle-aware ViewModel helpers, typed navigation, and
ready-to-customize loading and error surfaces—without forcing Hilt, Koin, or a specific app
architecture on you.

```text
Repository result → BaseViewModel → UIState → AppBaseScreen → Content / Loading / Error
                                      └──────→ one-shot effects
                                      └──────→ navigation commands
```

The result: less repeated glue code, fewer state bugs, and feature screens that are easier to read,
test, and evolve.

## The problems CoreUI solves

| Without a shared foundation | With CoreUI |
|---|---|
| Every screen invents its own loading and error flags | One `UIState<T>` models the complete screen lifecycle |
| Async calls repeat the same `try/catch`, loading, and retry code | `launchUiStateUpdate` owns the success/error lifecycle |
| ViewModels depend directly on `NavController` or expose ad-hoc events | `NavigationManager` keeps navigation command-based and decoupled |
| Route arguments are assembled and parsed as fragile strings | Typed path/query arguments build and read routes consistently |
| Loading, empty, dialog-error, and full-screen-error UI drift between features | `AppBaseScreen` applies the same behavior with opt-in customization |
| Infrastructure errors leak into UI code | `ResourceError` and `UiErrorMapper` produce presentation-ready errors |
| A foundation library dictates the app's DI framework | Constructor injection works; Hilt and Koin remain optional |

## Why teams adopt it

- **Ship screens faster** — start with the common state, async, error, and navigation decisions
  already made.
- **Make behavior predictable** — users get consistent loading, retry, and failure experiences
  across the app.
- **Reduce accidental complexity** — feature code focuses on domain data and user actions instead
  of coordinating booleans and one-off callbacks.
- **Keep architecture boundaries clean** — ViewModels issue navigation commands and expose state;
  Compose renders it.
- **Customize without forking** — replace the mapper, loading content, dialog, full-screen error,
  render policy, string resolver, or the entire navigation bridge.
- **Adopt incrementally** — use only the state/ViewModel layer, only the screen components, or the
  complete stack.

CoreUI is a good fit when you are building a multi-screen Compose app and want conventions without
adopting a heavyweight framework. It is intentionally less useful for a one-screen prototype or
for teams that already have an equivalent, mature presentation platform.

## What you get

- `UIState<T>` with explicit `IDLE`, `LOADING`, `SUCCESS`, and `ERROR` phases
- `BaseViewModel<UI, EVENT, EFFECT>` with coroutine safety, state updates, error mapping, retry,
  effects, and optional navigation
- `AppBaseScreen` for content, empty, loading, dialog-error, and full-screen-error rendering
- `Resource<T>` and extensible `ResourceError` categories for explicit result propagation
- `UiErrorMapper` for translating technical/domain failures into user-ready messages
- `NavigationManager`, typed routes, nested graph nodes, and an optional Compose navigation host
- No mandatory dependency-injection framework

## Contents

- [See the core workflow](#see-the-core-workflow)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Typed navigation](#typed-navigation)
- [Customization](#customization)
- [Architecture](#architecture)
- [Public API](#public-api)
- [Sample app](#sample-app)
- [Requirements](#requirements)
- [License](#license)

## See the core workflow

A feature only needs to describe its data, events, effects, and repository call. CoreUI coordinates
the repetitive lifecycle around them.

```kotlin
data class AlbumsUiModel(val albums: List<Album>)

sealed interface AlbumsEvent {
    data object Reload : AlbumsEvent
    data class AlbumSelected(val id: Long) : AlbumsEvent
}

sealed interface AlbumsEffect {
    data class ShowMessage(val message: String) : AlbumsEffect
}

class AlbumsViewModel(
    private val repository: AlbumRepository,
    navigationManager: NavigationManager
) : BaseViewModel<AlbumsUiModel, AlbumsEvent, AlbumsEffect>(navigationManager) {

    init {
        loadAlbums()
    }

    override fun handleEvent(event: AlbumsEvent) {
        when (event) {
            AlbumsEvent.Reload -> loadAlbums()
            is AlbumsEvent.AlbumSelected -> navigateToRoute("album/${event.id}")
        }
    }

    private fun loadAlbums() {
        launchUiStateUpdate(
            retryAction = ::loadAlbums,
            dataFetchBlock = repository::getAlbums,
            processSuccess = { AlbumsUiModel(albums = it) }
        )
    }
}
```

The screen stays focused on rendering and forwarding user intent:

```kotlin
@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppBaseScreen(
        uiState = uiState,
        errorDialogConfig = ErrorDialogConfig(
            onRetry = { viewModel.onEvent(AlbumsEvent.Reload) }
        ),
        emptyContent = { EmptyAlbums() },
        onErrorDialogDismiss = viewModel::dismissErrorPopup
    ) { model ->
        AlbumList(
            albums = model.albums,
            onAlbumClick = { viewModel.onEvent(AlbumsEvent.AlbumSelected(it)) }
        )
    }
}
```

`launchUiStateUpdate` automatically:

1. Moves the state to `LOADING`.
2. Executes the suspend repository call.
3. Publishes the mapped UI model and `SUCCESS` when it receives `Resource.Success`.
4. Maps `Resource.Error` to a `UIError`, attaches retry behavior, and publishes `ERROR`.

## Installation

CoreUI is published through GitHub Packages.

### 1. Authenticate with GitHub Packages

Add credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Create a GitHub personal access token with the `read:packages` scope. Do not commit it to the
repository.

### 2. Add the Maven repository

In the root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/androidexpert35/CoreUI")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                password = providers.gradleProperty("gpr.key").orNull
            }
        }
    }
}
```

### 3. Add the dependency

```kotlin
dependencies {
    implementation("com.tony.coreui:coreui:1.0.5")
}
```

## Quick start

### Initialize localized strings

When you use the default string resolver, initialize it once in your `Application`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreUiStringProvider.init(this)
    }
}
```

If you prefer avoiding global initialization, inject a `StringResolver` and `UiErrorMapper`
directly into your ViewModels.

### Model repository results explicitly

```kotlin
suspend fun getAlbums(): Resource<List<Album>> = try {
    Resource.Success(api.loadAlbums())
} catch (error: IOException) {
    Resource.Error(
        ResourceError.NetworkError(
            message = error.message.orEmpty()
        )
    )
}
```

This keeps infrastructure failures out of composables and gives the UI a single error path.

### Collect one-shot effects separately

State is for rendering; effects are for transient actions such as snackbars:

```kotlin
LaunchedEffect(viewModel) {
    viewModel.uiEffect.collectLatest { effect ->
        when (effect) {
            is AlbumsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }
}
```

## Typed navigation

CoreUI can keep route construction, argument parsing, and `NavController` out of your ViewModels.
The navigation layer is optional: skip it if your app already owns navigation.

### Define typed routes

```kotlin
object AppRoutes {
    val albumId = longPathArgument("albumId")
    val section = enumQueryArgument(
        name = "section",
        enumClass = DetailSection::class.java,
        defaultValue = DetailSection.OVERVIEW
    )

    val home = route("home")
    val album = route("album", albumId, section)
}
```

### Build graph nodes

```kotlin
val homeDestination = destinationNode(AppRoutes.home)
val albumDestination = destinationNode(AppRoutes.album)
val mainFlow = flowNode(route = "main", startDestination = homeDestination)
val root = rootNode(startDestination = mainFlow)
```

### Host the graph

```kotlin
@Composable
fun AppNavigator(navigationManager: NavigationManager) {
    CoreUiNavigator(
        navigationManager = navigationManager,
        root = root
    ) {
        flow(mainFlow) {
            destination(homeDestination) {
                HomeScreen()
            }

            destination(albumDestination) { entry ->
                AlbumScreen(
                    albumId = AppRoutes.album.requireArgument(entry, AppRoutes.albumId)
                )
            }
        }
    }
}
```

### Navigate without string assembly

```kotlin
navigationManager.navigate(
    AppRoutes.album,
    AppRoutes.albumId with 42L,
    AppRoutes.section with DetailSection.CUSTOMIZATION
)
```

## Customization

CoreUI provides useful defaults but keeps its main decisions replaceable.

### Loading and screen rendering

| Option | Behavior |
|---|---|
| `BaseLoadingType.DEFAULT` | Replaces content with the loading surface |
| `BaseLoadingType.OVERLAY` | Keeps content visible and displays loading above it |
| `BaseLoadingType.NONE` | Lets the host render its own loading experience |

`AppBaseScreen` also accepts custom `loadingScreen`, `emptyContent`, `errorDialog`, `errorScreen`,
and `contentWithState` composables. `BaseScreenRenderPolicy` controls system appearance, content
visibility while loading, and whether existing content remains visible on error.

### Error mapping

Use the default mapping for common failures or inject your own strategy:

```kotlin
class AppErrorMapper : UiErrorMapper {
    private val fallback = DefaultUiErrorMapper()

    override fun map(errorObject: Any, retryAction: (() -> Unit)?): UIError =
        fallback.map(errorObject, retryAction)

    override fun mapResourceError(
        resource: ResourceError?,
        retryAction: (() -> Unit)?
    ): UIError = when (resource) {
        is ResourceError.NetworkError -> UIError(
            title = "You're offline",
            message = "Check your connection and try again.",
            retryAction = retryAction,
            displayMode = UIErrorDisplayMode.FULL_SCREEN
        )
        else -> fallback.mapResourceError(resource, retryAction)
    }
}
```

You can also implement app-specific `ResourceError` types; the interface is intentionally open.

### Dependency injection

There is no mandatory DI dependency. Create `NavigationManagerImpl`, resolvers, and ViewModels with
plain constructor injection, or provide them through Hilt, Koin, or your existing container.

## Architecture

```text
┌────────────────────────────────────────────────────────┐
│ presentation                                           │
│ state · ViewModel · navigation · Compose components    │
├────────────────────────────────────────────────────────┤
│ data                                                   │
│ NavigationManagerImpl · string resolution              │
├────────────────────────────────────────────────────────┤
│ domain                                                 │
│ Resource<T> · ResourceError                            │
└────────────────────────────────────────────────────────┘
```

The layers are deliberately small. Apps may use the complete stack or depend only on the contracts
they need.

### Design principles

| Principle | Practical effect |
|---|---|
| One immutable state per screen | No scattered loading/error booleans |
| Events enter through `onEvent` | A visible, unidirectional UI contract |
| Effects use a separate `SharedFlow` | Transient actions are not replayed as screen state |
| Repositories return `Resource<T>` | Failure propagation is explicit |
| Navigation is command-based | ViewModels do not depend on `NavController` |
| Defaults are injectable or replaceable | Shared behavior does not block feature-specific UX |

## Public API

### State and ViewModel

| API | Purpose |
|---|---|
| `UIState<T>` | Screen data, status, error, and error-dialog visibility |
| `UIStatus` | `IDLE`, `LOADING`, `SUCCESS`, or `ERROR` |
| `UIError` | User-facing title, message, retry action, display mode, and metadata |
| `BaseViewModel<UI, EVENT, EFFECT>` | State, effects, async lifecycle, errors, and navigation helpers |
| `launchUiStateUpdate()` | Runs a `Resource`-producing operation and updates state consistently |

### Compose components

| API | Purpose |
|---|---|
| `AppBaseScreen` | Coordinates content, loading, empty, and error layers |
| `LoadingScreen` | Default full-screen or overlay loading feedback |
| `ErrorScreen` | Full-screen error surface with actions |
| `BaseDialog` | Default dialog error surface |
| `BaseScreenRenderPolicy` | Controls system appearance and layer visibility |

### Navigation

| API | Purpose |
|---|---|
| `NavigationManager` | Framework-facing navigation contract |
| `NavigationManagerImpl` | Default `SharedFlow`-based implementation |
| `RouteDefinition` / `RouteArgument<T>` | Typed route generation and argument extraction |
| `NavigationDestination` / `NavigationFlowNode` | Destination and nested-flow graph models |
| `CoreUiNavigator` | Optional Compose `NavHost` bridge |

### Results and errors

| API | Purpose |
|---|---|
| `Resource<T>` | Explicit `Success(data)` or `Error(ResourceError?)` result |
| `ResourceError` | Common network, service, validation, database, storage, and logic failures |
| `UiErrorMapper` | Strategy for converting failures into UI-ready messages |
| `DefaultUiErrorMapper` | Localized built-in mapping |

## Sample app

The [`sample`](sample) module is a runnable showcase rather than a collection of isolated snippets.
It demonstrates:

- default loading, empty, success, and dialog-error behavior
- a customized screen with overlay loading and full-screen errors
- state-driven filtering and one-shot snackbar effects
- typed path and enum query arguments
- nested graph nodes hosted by `CoreUiNavigator`
- constructor injection through a lightweight app container

Start with:

- [`LibraryViewModel.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/feature/library/LibraryViewModel.kt)
- [`LibraryScreen.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/feature/library/LibraryScreen.kt)
- [`DetailViewModel.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/feature/detail/DetailViewModel.kt)
- [`SampleNavigator.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/navigation/SampleNavigator.kt)

For a deeper architecture reference, see [`doc/AGENTS.md`](doc/AGENTS.md).

## Requirements

| Requirement | Version |
|---|---|
| Min SDK | API 29 (Android 10) |
| Compile SDK | API 36 |
| Java compatibility | 11 |
| Kotlin | 2.3.20 |
| Jetpack Compose BOM | 2026.03.01 |
| Navigation Compose | 2.9.7 |
| Lifecycle | 2.10.0 |
| Coroutines | 1.10.2 |

## License

CoreUI is available under the [MIT License](LICENSE).
