# CoreUI

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/androidexpert35/CoreUI/releases)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg)](https://android-arsenal.com/api?level=29)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF.svg)](https://kotlinlang.org)
[![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.03.01-4285F4.svg)](https://developer.android.com/jetpack/compose/bom)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Jetpack Compose presentation library for Android that provides the foundational building blocks for state-driven, event-based applications — typed state management, a base ViewModel, command-based navigation, and ready-to-use screen scaffolds.

---

## Contents

- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Getting Started](#getting-started)
  - [Initializing the Library](#initializing-the-library)
  - [State Management](#state-management)
  - [Building a ViewModel](#building-a-viewmodel)
  - [Typed Navigation](#typed-navigation)
  - [Screen Scaffold](#screen-scaffold)
  - [Error Handling](#error-handling)
- [Module Structure](#module-structure)
- [Public API](#public-api)
- [Sample App](#sample-app)
- [Requirements](#requirements)
- [License](#license)

---

## Features

- **Typed UI State** — `UIState<T>` with explicit IDLE / LOADING / SUCCESS / ERROR status phases
- **Base ViewModel** — abstract `BaseViewModel` with built-in coroutine safety, loading/error lifecycle, and effect emission
- **Command-Based Navigation** — `NavigationManager` decouples ViewModels from `NavController`; supports typed routes with path and query arguments
- **Screen Scaffold** — `AppBaseScreen` coordinates content, loading, and error layers with configurable policies
- **Structured Error Model** — seven `ResourceError` categories mapped automatically to user-facing `UIError`
- **No Mandatory DI** — constructor injection only; Hilt and Koin are both optional

---

## Architecture

CoreUI is layered to mirror clean architecture conventions:

```
┌──────────────────────────────────────────────────┐
│                  presentation/                   │
│                                                  │
│  state/          UIState · UIStatus · UIError    │
│  viewmodel/      BaseViewModel                   │
│  navigation/     NavigationManager · Routes      │
│  components/     AppBaseScreen · LoadingScreen   │
│                  ErrorScreen · BaseDialog        │
│  error/          UiErrorMapper                   │
├──────────────────────────────────────────────────┤
│                    data/                         │
│  navigation/     NavigationManagerImpl           │
│  strings/        CoreUiStringProvider            │
├──────────────────────────────────────────────────┤
│                   domain/                        │
│  resource/       Resource<T> · ResourceError     │
└──────────────────────────────────────────────────┘
```

**Key patterns:**

| Pattern | What it solves |
|---|---|
| `UIState<T>` as single source of truth | No scattered boolean flags for loading/error |
| `handleEvent(event)` as the only public ViewModel entry | Unidirectional data flow |
| `Resource<T>` (Success / Error) from domain | Explicit error propagation without exceptions |
| `RouteDefinition` + `RouteArgument<T>` | Compile-time-safe navigation arguments |
| `NavigationManager` interface | ViewModel navigates without touching `NavController` |

---

## Installation

The library is published to **GitHub Packages**.

### 1. Authenticate with GitHub Packages

Add your credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

> Generate a personal access token with the `read:packages` scope at **Settings → Developer settings → Personal access tokens**.

### 2. Add the Maven repository

In your root `settings.gradle.kts`:

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
    implementation("com.tony.coreui:coreui:1.0.0")
}
```

---

## Getting Started

### Initializing the Library

CoreUI's string resolver requires an `Application` context. Call `init` once in your `Application.onCreate()`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreUiStringProvider.init(this)
    }
}
```

---

### State Management

`UIState<T>` is the single data model passed to every screen:

```kotlin
data class UIState<T>(
    val status: UIStatus = UIStatus.IDLE,
    val data: T? = null,
    val error: UIError? = null,
    val showErrorDialog: Boolean = false
)

enum class UIStatus { IDLE, LOADING, SUCCESS, ERROR }
```

Define your screen's data model and plug it in:

```kotlin
data class AlbumListUiData(
    val albums: List<Album> = emptyList(),
    val selectedFilter: Filter = Filter.ALL
)

// In your ViewModel:
val uiState: StateFlow<UIState<AlbumListUiData>>
```

---

### Building a ViewModel

Extend `BaseViewModel` with three type parameters — the UI data model, a sealed class for events, and a sealed class for one-shot effects:

```kotlin
sealed class AlbumEvent {
    data object LoadAlbums : AlbumEvent()
    data class FilterChanged(val filter: Filter) : AlbumEvent()
}

sealed class AlbumEffect {
    data class ShowToast(val message: String) : AlbumEffect()
}

class AlbumViewModel(
    private val repository: AlbumRepository,
    navigationManager: NavigationManager
) : BaseViewModel<AlbumListUiData, AlbumEvent, AlbumEffect>(navigationManager) {

    override fun handleEvent(event: AlbumEvent) {
        when (event) {
            is AlbumEvent.LoadAlbums -> loadAlbums()
            is AlbumEvent.FilterChanged -> applyFilter(event.filter)
        }
    }

    private fun loadAlbums() {
        launchUiStateUpdate(
            action = { repository.getAlbums() },
            onSuccess = { albums ->
                AlbumListUiData(albums = albums)
            }
        )
    }

    private fun applyFilter(filter: Filter) {
        updateUiData { copy(selectedFilter = filter) }
        emitEffect(AlbumEffect.ShowToast("Filter: $filter"))
    }
}
```

`launchUiStateUpdate` automatically handles the full loading lifecycle:

1. Sets state to `LOADING`
2. Calls the suspend `action` lambda (returns `Resource<T>`)
3. On `Success` → calls `onSuccess`, transitions to `SUCCESS`
4. On `Error` → maps the error through `UiErrorMapper`, transitions to `ERROR`

**Navigation from a ViewModel:**

```kotlin
// Navigate to a typed route
navigateToRoute(AppRoutes.detail.createRoute(AppRoutes.albumId to 42L))

// Navigate up
navigateUp()

// Navigate and clear back stack
navigateAndClearBackstackTo(AppRoutes.home.routePattern)
```

---

### Typed Navigation

#### Define routes

```kotlin
object AppRoutes {
    // Simple route: /home
    val home = RouteDefinition("home")

    // Route with a path argument: /album/{albumId}
    val albumId = longPathArgument("albumId")
    val albumDetail = RouteDefinition("album", albumId)

    // Route with a query argument: /search?filter={filter}
    val filterArg = enumQueryArgument("filter", Filter::class.java, Filter.ALL)
    val search = RouteDefinition("search", filterArg)
}
```

#### Build the nav graph

```kotlin
@Composable
fun AppNavHost(navigationManager: NavigationManager) {
    CoreUiNavigator(
        startDestination = AppRoutes.home.routePattern,
        navigationManager = navigationManager
    ) {
        destination(AppRoutes.home) { HomeScreen() }

        destination(AppRoutes.albumDetail) { backStackEntry ->
            val id = AppRoutes.albumDetail.requireArgument(backStackEntry, AppRoutes.albumId)
            AlbumDetailScreen(albumId = id)
        }
    }
}
```

#### Create type-safe route strings

```kotlin
// Produces "album/42"
val route = AppRoutes.albumDetail.createRoute(AppRoutes.albumId to 42L)

// Produces "search?filter=RECENT"
val searchRoute = AppRoutes.search.createRoute(AppRoutes.filterArg to Filter.RECENT)
```

---

### Screen Scaffold

`AppBaseScreen` is the primary composable that wires up content, loading, and error layers automatically:

```kotlin
@Composable
fun AlbumListScreen(viewModel: AlbumViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppBaseScreen(
        uiState = uiState,
        loadingType = BaseLoadingType.DEFAULT,       // replaces content during load
        renderPolicy = BaseScreenRenderPolicy(),
        errorDialogConfig = ErrorDialogConfig(
            onRetry = { viewModel.handleEvent(AlbumEvent.LoadAlbums) },
            onDismiss = { viewModel.dismissErrorPopup() }
        )
    ) { state ->
        AlbumListContent(data = state.data)
    }
}
```

**Loading types:**

| `BaseLoadingType` | Behavior |
|---|---|
| `DEFAULT` | Replaces content with `LoadingScreen` |
| `OVERLAY` | Shows spinner over content |
| `NONE` | Caller manages loading display |

**`BaseScreenRenderPolicy` controls:**
- System bar appearance (status bar / navigation bar colors and icon tint)
- Whether content is hidden while loading
- Whether error state persists after dismissal

---

### Error Handling

#### Domain errors

Return `Resource.Error(ResourceError)` from your repository layer:

```kotlin
sealed interface ResourceError {
    data class NetworkError(val message: String, val httpCode: Int? = null) : ResourceError
    data class ServiceError(val message: String, val errorCode: String? = null) : ResourceError
    data class LogicError(val errorMessage: String, val errorCode: String? = null) : ResourceError
    data class ValidationError(val message: String, val field: String? = null) : ResourceError
    data class DatabaseError(val message: String) : ResourceError
    data class StorageError(val message: String) : ResourceError
    data object UnknownError : ResourceError
}
```

#### Custom error mapping

Implement `UiErrorMapper` to override how domain errors become UI messages:

```kotlin
class MyUiErrorMapper : UiErrorMapper {
    override fun mapResourceError(error: ResourceError?): UIError {
        return when (error) {
            is ResourceError.NetworkError -> UIError(
                title = "No connection",
                message = "Check your internet and try again.",
                displayMode = UIErrorDisplayMode.FULL_SCREEN
            )
            else -> DefaultUiErrorMapper().mapResourceError(error)
        }
    }
}
```

Pass your mapper to the ViewModel:

```kotlin
class MyViewModel(
    repo: MyRepository,
    nav: NavigationManager
) : BaseViewModel<MyUiData, MyEvent, MyEffect>(
    navigationManager = nav,
    errorMapper = MyUiErrorMapper()
)
```

---

## Module Structure

```
CoreUI/
├── coreui/                          # Library module (AAR)
│   └── src/main/java/com/tony/coreui/
│       ├── domain/
│       │   └── resource/            # Resource<T>, ResourceError
│       ├── data/
│       │   ├── navigation/          # NavigationManagerImpl
│       │   └── strings/             # CoreUiStringProvider, AndroidStringResolver
│       └── presentation/
│           ├── state/               # UIState, UIStatus, UIError, UIErrorDisplayMode
│           ├── viewmodel/           # BaseViewModel
│           ├── navigation/          # NavigationManager, commands, routes, graph DSL
│           ├── components/
│           │   └── basescreen/      # AppBaseScreen, LoadingScreen, ErrorScreen, BaseDialog
│           └── error/               # UiErrorMapper, DefaultUiErrorMapper
│
├── sample/                          # Demo application
│   └── src/main/java/com/tony/coreui/sample/
│       ├── app/                     # Application, MainActivity, DI container
│       ├── domain/                  # Demo models (DemoAlbum, filters, sections)
│       ├── data/                    # FakeShowcaseRepository
│       └── presentation/
│           ├── feature/             # Library screen, Detail screen + ViewModels
│           ├── navigation/          # SampleRoutes (typed route definitions)
│           └── theme/               # CoreUiSampleTheme
│
├── gradle/
│   └── libs.versions.toml           # Version catalog
└── doc/
    └── AGENTS.md                    # Detailed architecture reference
```

---

## Public API

### State

| Type | Description |
|---|---|
| `UIState<T>` | Screen state container: status, data, error |
| `UIStatus` | `IDLE` · `LOADING` · `SUCCESS` · `ERROR` |
| `UIError` | User-facing error with title, message, display mode, and optional retry action |
| `UIErrorDisplayMode` | `DIALOG` · `FULL_SCREEN` · `NONE` |

### ViewModel

| Type / Method | Description |
|---|---|
| `BaseViewModel<UI, EVENT, EFFECT>` | Abstract base; implement `handleEvent()` |
| `launchUiStateUpdate()` | Fetch data with automatic loading/error lifecycle |
| `updateUiData()` | Partial state update without changing status |
| `emitEffect()` | Fire a one-shot side effect |
| `navigateToRoute()` · `navigateUp()` · `navigateAndClearBackstackTo()` | Navigation helpers |

### Navigation

| Type | Description |
|---|---|
| `NavigationManager` | Interface for issuing navigation commands |
| `NavigationManagerImpl` | Default `SharedFlow`-based implementation |
| `NavigationCommand` | `Navigate` · `NavigateUp` · `PopBackStack` · `NavigateAndClearBackStack` |
| `RouteDefinition` | Typed route with pattern generation and argument extraction |
| `RouteArgument<T>` | Factory functions: `stringPathArgument()`, `longPathArgument()`, `intPathArgument()`, `booleanPathArgument()`, `floatPathArgument()`, `enumPathArgument()` — plus query variants |
| `CoreUiNavigator` | Composable `NavHost` wrapper that consumes `NavigationManager` commands |

### Components

| Composable | Description |
|---|---|
| `AppBaseScreen` | Primary screen scaffold; coordinates content / loading / error layers |
| `LoadingScreen` | Full-screen or overlay loading indicator with optional label |
| `ErrorScreen` | Full-screen error with up to three action buttons |
| `BaseDialog` | Alert dialog with title, message, confirm, retry, and cancel buttons |

### Domain

| Type | Description |
|---|---|
| `Resource<T>` | `Success(data)` or `Error(ResourceError?)` |
| `ResourceError` | Sealed interface: `NetworkError`, `ServiceError`, `LogicError`, `ValidationError`, `DatabaseError`, `StorageError`, `UnknownError` |

### Error Mapping

| Type | Description |
|---|---|
| `UiErrorMapper` | Interface — implement to customize domain → UI error translation |
| `DefaultUiErrorMapper` | Built-in mapper using library string resources |

---

## Sample App

The `:sample` module demonstrates the library end-to-end with two screens:

**Library Screen** (`/library?filter={filter}`)
- Loads a list of demo albums from an in-memory fake repository
- Demonstrates `launchUiStateUpdate`, filter events via `handleEvent`, and enum query arguments

**Detail Screen** (`/album/{albumId}?section={section}`)
- Receives a Long path argument and an enum query argument
- Shows a custom `UiErrorMapper` for screen-specific error messages
- Demonstrates `OVERLAY` loading and `FULL_SCREEN` error mode

Clone the repository and run the `sample` configuration in Android Studio to explore the patterns interactively. Start with these files for the quickest walkthrough:

- [`LibraryViewModel.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/feature/library/LibraryViewModel.kt)
- [`LibraryScreen.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/feature/library/LibraryScreen.kt)
- [`DetailViewModel.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/feature/detail/DetailViewModel.kt)
- [`SampleRoutes.kt`](sample/src/main/java/com/tony/coreui/sample/presentation/navigation/SampleRoutes.kt)

For a deep dive into every type and pattern, see [AGENTS.md](doc/AGENTS.md).

---

## Requirements

| | |
|---|---|
| Min SDK | API 29 (Android 10) |
| Compile SDK | API 36 |
| Kotlin | 2.3.20 |
| Jetpack Compose BOM | 2026.03.01 |
| Navigation Compose | 2.9.7 |
| Lifecycle | 2.10.0 |
| Coroutines | 1.10.2 |
| Java compatibility | 11 |

---

## License

```
MIT License

Copyright (c) 2026 Tony

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
