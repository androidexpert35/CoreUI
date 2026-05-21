# CoreUI — Agent Reference

> **Who this file is for**: AI coding agents (Claude, Copilot, Cursor, etc.) working in this repository.
> It provides everything needed to correctly use, extend, or generate code against the CoreUI library
> without hallucinating APIs or misusing patterns.

---

## Table of Contents

1. [What CoreUI Is](#1-what-coreui-is)
2. [Module Structure](#2-module-structure)
3. [Package Map](#3-package-map)
4. [Architecture Overview](#4-architecture-overview)
5. [Layer: Domain](#5-layer-domain)
6. [Layer: Data](#6-layer-data)
7. [Layer: Presentation — State](#7-layer-presentation--state)
8. [Layer: Presentation — BaseViewModel](#8-layer-presentation--baseviewmodel)
9. [Layer: Presentation — Navigation](#9-layer-presentation--navigation)
10. [Layer: Presentation — UI Components](#10-layer-presentation--ui-components)
11. [String Resolution](#11-string-resolution)
12. [Error Mapping](#12-error-mapping)
13. [Complete Feature Walkthrough](#13-complete-feature-walkthrough)
14. [Rules and Anti-Patterns](#14-rules-and-anti-patterns)
15. [Dependency Graph](#15-dependency-graph)

---

## 1. What CoreUI Is

CoreUI is an **Android Jetpack Compose library** (`com.tony.coreui`) that provides:

- A typed **state management** system (`UIState`, `UIStatus`, `UIError`)
- A **base ViewModel** with built-in loading/error/success transitions and coroutine safety
- A **command-based navigation** system decoupled from NavController
- **Reusable Compose scaffolds** for screens (loading, error, content layers)
- A **typed route definition** system with path and query arguments
- A **resource wrapper** (`Resource<T>`) for domain results

**Target SDK**: 36 · **Min SDK**: 29 · **Kotlin**: 2.3.20 · **Compose BOM**: 2026.03.01

**No mandatory DI framework.** Everything is constructor-injectable. Hilt/Koin are optional.

---

## 2. Module Structure

```
CoreUI/
├── app/                  # Demo application (not part of the library artifact)
└── coreui/               # Library module → produces coreui.aar
    └── src/main/
        ├── java/com/tony/coreui/
        └── res/values/strings.xml
```

The `:coreui` module is the **only** module agents should read or write when working on the library.

---

## 3. Package Map

```
com.tony.coreui
│
├── domain/
│   └── resource/
│       ├── Resource.kt              — sealed class Success/Error + extension functions
│       └── ResourceError.kt         — sealed interface with 7 error categories
│
├── data/
│   ├── navigation/
│   │   └── NavigationManagerImpl.kt — SharedFlow-backed NavigationManager
│   └── strings/
│       ├── CoreUiStringProvider.kt  — global singleton StringResolver
│       └── StringResolver.kt        — interface + AndroidStringResolver
│
└── presentation/
    ├── state/
    │   ├── UIState.kt               — data class wrapping status + data + error
    │   ├── UIStatus.kt              — enum: IDLE, LOADING, SUCCESS, ERROR
    │   ├── UIError.kt               — error model for UI layer
    │   └── UIErrorDisplayMode.kt    — enum: DIALOG, FULL_SCREEN, NONE
    ├── viewmodel/
    │   └── BaseViewModel.kt         — abstract base ViewModel
    ├── error/
    │   └── UiErrorMapper.kt         — interface + DefaultUiErrorMapper
    ├── navigation/
    │   ├── NavigationCommand.kt     — sealed interface of navigation intents
    │   ├── NavigationManager.kt     — interface consumed by ViewModel + Compose
    │   ├── NavigationOptions.kt     — data class for navigate() options
    │   ├── route/
    │   │   ├── RouteDefinition.kt   — typed route with path/query arguments
    │   │   ├── RouteArgument.kt     — typed argument + helper factory functions
    │   │   └── RouteValueType.kt    — serialization strategy per type
    │   ├── graph/
    │   │   ├── NavigationNode.kt    — NavigationDestination / FlowNode / RootNode
    │   │   └── NavGraphBuilderExtensions.kt
    │   └── compose/
    │       ├── CoreUiNavigator.kt   — top-level NavHost Composable
    │       └── NavigationCommandBridge.kt
    └── components/
        └── basescreen/
            ├── AppBaseScreen.kt     — main screen scaffold Composable
            ├── LoadingScreen.kt
            ├── ErrorScreen.kt
            ├── BaseDialog.kt
            ├── BaseLoadingType.kt
            ├── ErrorDialogConfig.kt
            ├── BaseScreenRenderPolicy.kt
            ├── BaseScreenStateResolver.kt  (internal)
            ├── BaseScreenLayers.kt         (internal)
            └── SystemAppearance.kt
```

---

## 4. Architecture Overview

```
┌──────────────────────────────────────────────────┐
│                  PRESENTATION                    │
│  ┌────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │BaseViewModel│  │ AppBaseScreen│  │Navigator │ │
│  │(StateFlow) │  │  Composable  │  │Composable│ │
│  └─────┬──────┘  └──────┬───────┘  └────┬─────┘ │
│        │ UIState<T>     │               │        │
└────────┼────────────────┼───────────────┼────────┘
         │                │               │ NavigationCommand
┌────────┼────────────────┼───────────────┼────────┐
│                     DATA                         │
│  ┌─────▼──────┐                  ┌──────▼──────┐ │
│  │ NavigationManagerImpl         │StringProvider│ │
│  └────────────┘                  └─────────────┘ │
└──────────────────────────────────────────────────┘
         │ Resource<T>
┌────────┼─────────────────────────────────────────┐
│                    DOMAIN                        │
│  ┌─────▼──────┐  ┌──────────────┐               │
│  │ Resource<T>│  │ResourceError │               │
│  └────────────┘  └──────────────┘               │
└──────────────────────────────────────────────────┘
```

**Flow**: Repository returns `Resource<T>` → ViewModel wraps it into `UIState<T>` →
Composable renders `AppBaseScreen` driven by `UIState` → User actions become
`UI_EVENT` sent to `handleEvent()` → ViewModel emits `NavigationCommand` via
`NavigationManager` → `NavigationCommandBridge` translates to NavController calls.

---

## 5. Layer: Domain

### `Resource<T>`

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val error: ResourceError? = null) : Resource<Nothing>()
}
```

**Extension functions** (all inline):

| Function | Signature | Description |
|---|---|---|
| `map` | `Resource<T>.map { T -> R } : Resource<R>` | Transform success data |
| `fold` | `Resource<T>.fold(onSuccess, onError) : R` | Unwrap either branch |
| `onSuccess` | `Resource<T>.onSuccess { T } : Resource<T>` | Side-effect on success |
| `onError` | `Resource<T>.onError { ResourceError? } : Resource<T>` | Side-effect on error |
| `getOrNull` | `Resource<T>.getOrNull() : T?` | Null on error |

### `ResourceError`

```kotlin
sealed interface ResourceError {
    data class LogicError(val errorMessage: String?, val errorCode: String?) : ResourceError
    data class ValidationError(val message: String?, val field: String?) : ResourceError
    data class DatabaseError(val message: String?) : ResourceError
    data class StorageError(val message: String?) : ResourceError
    data class ServiceError(val message: String?, val errorCode: String?) : ResourceError
    data class NetworkError(val message: String?, val httpCode: Int?) : ResourceError
    data object UnknownError : ResourceError
}
```

Use `ResourceError` as the domain-level error type. `DefaultUiErrorMapper` knows how to
turn each subtype into a user-facing `UIError`.

---

## 6. Layer: Data

### `NavigationManagerImpl`

The concrete implementation of `NavigationManager`. Internally uses:
- `MutableSharedFlow<NavigationCommand>` for commands
- `MutableStateFlow<String?>` for `currentRoute`

**Instantiation** (done by the host app, injected into ViewModels):

```kotlin
val navigationManager: NavigationManager = NavigationManagerImpl()
```

### `CoreUiStringProvider`

A global singleton that implements `StringResolver`. Must be initialized **once** at
app startup before any ViewModel is created.

```kotlin
// In Application.onCreate() or DI graph setup:
CoreUiStringProvider.init(context)
```

`StringResolver` interface:

```kotlin
interface StringResolver {
    fun getString(@StringRes id: Int, vararg args: Any): String
}
```

---

## 7. Layer: Presentation — State

### `UIState<T>`

```kotlin
data class UIState<T>(
    val status: UIStatus = UIStatus.IDLE,
    val data: T? = null,
    val error: UIError? = null,
    val showErrorDialog: Boolean = false
)
```

`T` is the **UI model** (the data needed to render the screen). Can be any data class or
even `Unit` for screens with no data.

### `UIStatus`

```kotlin
enum class UIStatus { IDLE, LOADING, SUCCESS, ERROR }
```

### `UIError`

```kotlin
data class UIError(
    val title: String,
    val message: String,
    val type: UIErrorType,               // NETWORK, VALIDATION, SERVICE, DATABASE, STORAGE, LOGIC, GENERIC
    val retryAction: (() -> Unit)? = null,
    val displayMode: UIErrorDisplayMode = UIErrorDisplayMode.DIALOG,
    val metadata: Map<String, Any> = emptyMap()
)
```

### `UIErrorDisplayMode`

```kotlin
enum class UIErrorDisplayMode { DIALOG, FULL_SCREEN, NONE }
```

`DIALOG` → renders `BaseDialog` via `AppBaseScreen`.
`FULL_SCREEN` → renders `ErrorScreen` via `AppBaseScreen`.
`NONE` → error is present in state but the library renders nothing (host handles it).

---

## 8. Layer: Presentation — BaseViewModel

### Class Signature

```kotlin
abstract class BaseViewModel<UI_TYPE, UI_EVENT, UI_EFFECT>(
    navigationManager: NavigationManager? = null,
    stringResolver: StringResolver = CoreUiStringProvider,
    uiErrorMapper: UiErrorMapper = DefaultUiErrorMapper(stringResolver)
) : ViewModel()
```

**Type parameters**:
- `UI_TYPE` — the UI data model for this screen (e.g. `AlbumDetailUiModel`)
- `UI_EVENT` — sealed class/interface for user events (e.g. `AlbumDetailEvent`)
- `UI_EFFECT` — sealed class/interface for one-shot effects (e.g. `AlbumDetailEffect`)

### Properties

```kotlin
val uiState: StateFlow<UIState<UI_TYPE>>   // observed by Composable
val uiEffect: SharedFlow<UI_EFFECT>         // one-shot side effects (snackbar, navigation signal, etc.)
```

### Public Entry Point

```kotlin
fun onEvent(event: UI_EVENT)          // public — called by the Composable
```

```kotlin
protected abstract fun handleEvent(event: UI_EVENT)   // implement in subclass
```

`onEvent` is the **single public entry point** for all UI interactions; it delegates to
`handleEvent`. Implement `handleEvent` with a `when` on the sealed event type.

### State Mutation Methods (protected)

| Method | When to use |
|---|---|
| `setLoadingState()` | Before async work starts |
| `setSuccessState(newData)` | When data is ready |
| `setIdleState(newData)` | Reset to idle (e.g. after completing a non-data action) |
| `setErrorState(error)` | Propagate a `UIError` |
| `updateUiData(newData)` | Patch only the data field without changing status |
| `updateUiState { UIState -> UIState }` | Full state transform |
| `emitEffect(effect)` | Emit a one-shot effect |
| `showErrorPopup(true/false)` | Toggle dialog visibility |
| `dismissErrorPopup()` | Hide error dialog |

### `launchUiStateUpdate` — The Primary Data-Fetch Pattern

```kotlin
protected fun <RESOURCE> launchUiStateUpdate(
    retryAction: (() -> Unit)? = null,
    dataFetchBlock: suspend () -> Resource<RESOURCE>,
    processSuccess: (RESOURCE) -> UI_TYPE,
    updateUiAfterError: ((UIError) -> UI_TYPE?)? = null,
    invokeOnCompletion: ((success: Boolean) -> Unit)? = null,
    skipLoading: Boolean = false
)
```

This method:
1. Optionally sets LOADING state (unless `skipLoading = true`)
2. Calls `dataFetchBlock` in `viewModelScope`
3. On `Resource.Success` → calls `processSuccess` → calls `setSuccessState`
4. On `Resource.Error` → maps error → calls `setErrorState` (optionally updates data via `updateUiAfterError`)
5. Always calls `invokeOnCompletion` at the end

**Typical usage**:

```kotlin
fun loadAlbum(id: Long) = launchUiStateUpdate(
    retryAction = { loadAlbum(id) },
    dataFetchBlock = { repository.getAlbum(id) },
    processSuccess = { album -> album.toUiModel() }
)
```

### Navigation Methods (public)

```kotlin
fun navigateToRoute(route: String, options: NavigationOptions = NavigationOptions())
fun navigateUp()
fun popBackStack(route: String? = null, inclusive: Boolean = false)
fun navigateAndClearBackstackTo(route: String, popUpToRoute: String? = null, inclusive: Boolean = true)
```

These delegate to the injected `NavigationManager`. They are no-ops if `navigationManager`
was not provided.

### `handleError` — Manual Error Handling

```kotlin
protected open fun handleError(
    errorObject: Any,                               // Throwable, ResourceError, or any
    retryAction: (() -> Unit)? = null,
    processUiAfterError: ((UIError) -> UI_TYPE?)? = null,
    processUIError: ((UIError) -> UIError)? = null  // mutate the mapped error before storing
)
```

Use when you need to handle errors outside `launchUiStateUpdate` (e.g. inside `executeAsync`).

### `executeAsync` — Fire-and-Forget Coroutine

```kotlin
protected inline fun executeAsync(crossinline block: suspend () -> Unit)
```

Runs `block` in `viewModelScope`. Does **not** manage UIState. Use for side effects
(analytics, caching, etc.) that don't need to reflect in the UI state.

---

## 9. Layer: Presentation — Navigation

### Core Types

```kotlin
// Commands emitted by ViewModel, consumed by NavigationCommandBridge
sealed interface NavigationCommand {
    data class Navigate(val route: String, val options: NavigationOptions) : NavigationCommand
    data object NavigateUp : NavigationCommand
    data class PopBackStack(val route: String?, val inclusive: Boolean) : NavigationCommand
    data class NavigateAndClearBackStack(val route: String, val popUpToRoute: String?, val inclusive: Boolean) : NavigationCommand
}

// Options for Navigate command
data class NavigationOptions(
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
    val popUpToRoute: String? = null,
    val popUpToInclusive: Boolean = false,
    val allowRepeatOnSameRoute: Boolean = false,
    val extras: Map<String, Any?> = emptyMap()
)
```

### `NavigationManager` Interface

```kotlin
interface NavigationManager {
    val navigationCommands: SharedFlow<NavigationCommand>
    val currentRoute: StateFlow<String?>
    fun navigate(route: String, options: NavigationOptions = NavigationOptions())
    fun navigateUp()
    fun popBackStack(route: String? = null, inclusive: Boolean = false)
    fun navigateAndClearBackStack(route: String, popUpToRoute: String? = null, inclusive: Boolean = true)
}
```

### Typed Route Definitions

#### `RouteDefinition`

```kotlin
class RouteDefinition(
    val baseRoute: String,
    arguments: List<RouteArgument<*>> = emptyList()
)
```

Properties:
- `routePattern: String` — navigation pattern with placeholders, e.g. `"album/{albumId}?filter={filter}"`
- `navArguments: List<NamedNavArgument>` — ready for use in `composable()` / `navigation()`

Methods:
- `createRoute(vararg values: RouteValue<*>): String` — builds the concrete route string, e.g. `"album/42?filter=recent"`
- `<T> requireArgument(entry: NavBackStackEntry, arg: RouteArgument<T>): T` — extracts and throws if absent
- `<T> getArgument(entry: NavBackStackEntry, arg: RouteArgument<T>): T?` — extracts or returns null

#### `RouteArgument<T>`

```kotlin
data class RouteArgument<T>(
    val name: String,
    val location: ArgumentLocation,   // PATH or QUERY
    val valueType: RouteValueType<T>,
    val nullable: Boolean = false,
    val defaultValue: T? = null
)
```

#### Factory Functions

```kotlin
// Path arguments (required, non-nullable)
fun stringPathArgument(name: String): RouteArgument<String>
fun longPathArgument(name: String): RouteArgument<Long>
fun intPathArgument(name: String): RouteArgument<Int>
fun booleanPathArgument(name: String): RouteArgument<Boolean>
fun floatPathArgument(name: String): RouteArgument<Float>
fun <T : Enum<T>> enumPathArgument(name: String, enumClass: Class<T>): RouteArgument<T>
fun <T> customPathArgument(name: String, valueType: RouteValueType<T>): RouteArgument<T>

// Query arguments (optional, nullable/defaultable)
fun stringQueryArgument(name: String, nullable: Boolean = false, defaultValue: String? = null): RouteArgument<String>
fun longQueryArgument(name: String, nullable: Boolean = false, defaultValue: Long? = null): RouteArgument<Long>
// ...same pattern for Int, Boolean, Float, Enum, custom
```

#### Convenience top-level builder

```kotlin
fun route(baseRoute: String, vararg arguments: RouteArgument<*>): RouteDefinition
```

#### `RouteValue` and infix `with`

```kotlin
// Pair an argument with its concrete value for createRoute()
infix fun <T> RouteArgument<T>.with(value: T?): RouteValue<T>
```

**Full Example**:

```kotlin
// Define once (e.g. in a Routes object)
object Routes {
    val albumId = longPathArgument("albumId")
    val filter  = stringQueryArgument("filter", nullable = true)
    val album   = route("album", albumId, filter)
}

// Build concrete route string
val route = Routes.album.createRoute(
    Routes.albumId with 42L,
    Routes.filter  with "recent"
)
// → "album/42?filter=recent"

// Navigate
viewModel.navigateToRoute(route)

// Extract arguments in destination Composable
composable(
    route = Routes.album.routePattern,
    arguments = Routes.album.navArguments
) { entry ->
    val id     = Routes.album.requireArgument(entry, Routes.albumId) // Long
    val filter = Routes.album.getArgument(entry, Routes.filter)      // String?
}
```

### Navigation Graph Nodes

```kotlin
// A single screen destination — route is derived from routeDefinition.routePattern
data class NavigationDestination(val routeDefinition: RouteDefinition) : NavigationNode

// A nested nav graph (flow)
data class NavigationFlowNode(
    override val route: String,
    val startDestination: NavigationDestination
) : NavigationNode

// The root node of the entire graph
data class NavigationRootNode(
    override val route: String = "root",
    val startDestination: NavigationNode           // accepts Destination or FlowNode
) : NavigationNode

// Factory helpers
fun destinationNode(routeDefinition: RouteDefinition): NavigationDestination
fun flowNode(route: String, startDestination: NavigationDestination): NavigationFlowNode
fun rootNode(startDestination: NavigationNode, route: String = "root"): NavigationRootNode
```

### NavGraphBuilder Extensions

```kotlin
// Register a single screen
fun NavGraphBuilder.destination(
    destination: NavigationDestination,
    content: @Composable (NavBackStackEntry) -> Unit
)

// Register a nested flow (navigation graph)
fun NavGraphBuilder.flow(
    flow: NavigationFlowNode,
    builder: NavGraphBuilder.() -> Unit
)
```

### `CoreUiNavigator` Composable

```kotlin
@Composable
fun CoreUiNavigator(
    navigationManager: NavigationManager,
    root: NavigationRootNode,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    builder: NavGraphBuilder.() -> Unit
)
```

This is the **top-level entry point** for navigation. Place it once in your activity/root
Composable. It embeds `NavigationCommandBridge` internally.

---

## 10. Layer: Presentation — UI Components

### `AppBaseScreen<T>`

The primary screen scaffold. Handles loading/error/content orchestration automatically.

```kotlin
@Composable
fun <T> AppBaseScreen(
    uiState: UIState<T>,
    statusBarColor: Color = MaterialTheme.colorScheme.surface,
    navigationBarColor: Color = statusBarColor,
    useLightStatusIcons: Boolean? = null,
    useLightNavigationIcons: Boolean? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    renderPolicy: BaseScreenRenderPolicy = BaseScreenRenderPolicy(),
    errorDialogConfig: ErrorDialogConfig = ErrorDialogConfig(),
    loadingType: BaseLoadingType = BaseLoadingType.DEFAULT,
    loadingScreen: (@Composable () -> Unit)? = null,          // override default LoadingScreen
    emptyContent: (@Composable () -> Unit)? = null,           // rendered when data is null + SUCCESS
    errorDialog: (@Composable (UIError, () -> Unit) -> Unit)? = null,  // override default dialog
    errorScreen: (@Composable (UIError) -> Unit)? = null,     // override default ErrorScreen
    contentWithState: (@Composable (T, UIState<T>) -> Unit)? = null,   // content + full state access
    onErrorDialogDismiss: () -> Unit = {},
    dialogProperties: DialogProperties = DialogProperties(...),
    content: @Composable (T) -> Unit                          // content with data only (preferred)
)
```

**Rendering rules** (based on `uiState.status` and `UIErrorDisplayMode`):

| Status | Error displayMode | What renders |
|---|---|---|
| `IDLE` / `SUCCESS` | — | `content(data)` or `emptyContent()` if data is null |
| `LOADING` (DEFAULT) | — | `LoadingScreen` (content hidden unless `renderPolicy.hideContentOnDefaultLoading = false`) |
| `LOADING` (OVERLAY) | — | Content + loading overlay on top |
| `LOADING` (NONE) | — | Content only (host handles loading UI) |
| `ERROR` | `DIALOG` | Content (if data exists) + `BaseDialog` |
| `ERROR` | `FULL_SCREEN` | `ErrorScreen` |
| `ERROR` | `NONE` | Content only (host handles error UI) |

### `BaseScreenRenderPolicy`

```kotlin
data class BaseScreenRenderPolicy(
    val applySystemAppearance: Boolean = true,
    val hideContentOnDefaultLoading: Boolean = true,
    val keepContentVisibleOnError: Boolean = true    // only relevant for FULL_SCREEN errors
)
```

### `ErrorDialogConfig`

```kotlin
data class ErrorDialogConfig(
    val onConfirm: () -> Unit = {},
    val onRetry: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,     // invoked after any dismiss action
    val confirmButtonText: String? = null,           // overrides default "OK"
    val retryButtonText: String? = null,
    val dismissButtonText: String? = null
)
```

If `onRetry` is null and `UIError.retryAction` is non-null, the retry action from the
error is used automatically.

### `BaseLoadingType`

```kotlin
enum class BaseLoadingType { DEFAULT, OVERLAY, NONE }
```

### `LoadingScreen`

```kotlin
@Composable
fun LoadingScreen(
    loadingText: String = stringResource(R.string.coreui_loading_label),
    progress: Int? = null,             // null → indeterminate; 0-100 → determinate
    backgroundColor: Color = MaterialTheme.colorScheme.background
)
```

### `ErrorScreen`

```kotlin
@Composable
fun ErrorScreen(
    title: String,
    description: String,
    primaryButtonText: String? = null,
    onPrimaryButtonClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
    tertiaryButtonText: String? = null,
    onTertiaryButtonClick: (() -> Unit)? = null
)
```

### `BaseDialog`

```kotlin
@Composable
fun BaseDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dismissButtonText: String? = null,
    onCancel: (() -> Unit)? = null,
    retryButtonText: String? = null,
    onRetry: (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
)
```

---

## 11. String Resolution

The library resolves its internal strings via `StringResolver`. The default implementation
is `CoreUiStringProvider` (a global singleton).

**Required setup** (once, before any ViewModel is created):

```kotlin
CoreUiStringProvider.init(applicationContext)
```

If you want per-ViewModel string resolution (e.g. for testing):

```kotlin
class MyViewModel(
    stringResolver: StringResolver = CoreUiStringProvider
) : BaseViewModel<...>(stringResolver = stringResolver)
```

For tests, inject a `FakeStringResolver`:

```kotlin
class FakeStringResolver : StringResolver {
    override fun getString(id: Int, vararg args: Any) = "test_string_$id"
}
```

---

## 12. Error Mapping

### `UiErrorMapper` Interface

```kotlin
interface UiErrorMapper {
    fun map(error: Any, retryAction: (() -> Unit)? = null): UIError
}
```

### `DefaultUiErrorMapper`

Maps the following input types:
- `Throwable` → `UIError` with generic or network title/message
- `ResourceError.NetworkError` → title from `coreui_error_network_title`, displayMode = DIALOG
- `ResourceError.ServiceError` → title from `coreui_error_service_title`
- `ResourceError.ValidationError` → displayMode = DIALOG, includes field info
- `ResourceError.DatabaseError` / `StorageError` → storage-category error
- `ResourceError.LogicError` → logic-category error
- `ResourceError.UnknownError` → generic fallback
- Any other object → `UIError` with `UIErrorType.GENERIC`

**Custom mapper** example:

```kotlin
class AppUiErrorMapper(resolver: StringResolver) : UiErrorMapper {
    private val delegate = DefaultUiErrorMapper(resolver)

    override fun map(error: Any, retryAction: (() -> Unit)?): UIError {
        return when (error) {
            is MyDomainError.SessionExpired -> UIError(
                title = "Session expired",
                message = "Please log in again.",
                type = UIErrorType.LOGIC,
                displayMode = UIErrorDisplayMode.FULL_SCREEN
            )
            else -> delegate.map(error, retryAction)
        }
    }
}
```

Inject via:

```kotlin
class MyViewModel(mapper: UiErrorMapper) : BaseViewModel<...>(uiErrorMapper = mapper)
```

---

## 13. Complete Feature Walkthrough

Below is a minimal but complete example of adding a new screen using all CoreUI layers.

### Step 1 — Domain: define the result type

```kotlin
// In your domain layer (outside CoreUI)
data class Album(val id: Long, val title: String, val trackCount: Int)
```

### Step 2 — Domain: use Resource<T> in repository

```kotlin
interface AlbumRepository {
    suspend fun getAlbum(id: Long): Resource<Album>
}
```

### Step 3 — Presentation: define UI model, events, effects

```kotlin
data class AlbumUiModel(val title: String, val tracks: String)

sealed interface AlbumEvent {
    data class Load(val id: Long) : AlbumEvent
    data object RetryClicked : AlbumEvent
    data object BackClicked : AlbumEvent
}

sealed interface AlbumEffect {
    data class ShowToast(val message: String) : AlbumEffect
}
```

### Step 4 — ViewModel

```kotlin
class AlbumViewModel(
    private val repository: AlbumRepository,
    navigationManager: NavigationManager
) : BaseViewModel<AlbumUiModel, AlbumEvent, AlbumEffect>(
    navigationManager = navigationManager
) {
    private var lastAlbumId: Long = -1

    override fun handleEvent(event: AlbumEvent) = when (event) {
        is AlbumEvent.Load       -> loadAlbum(event.id)
        is AlbumEvent.RetryClicked -> loadAlbum(lastAlbumId)
        is AlbumEvent.BackClicked  -> navigateUp()
    }

    private fun loadAlbum(id: Long) {
        lastAlbumId = id
        launchUiStateUpdate(
            retryAction = { loadAlbum(id) },
            dataFetchBlock = { repository.getAlbum(id) },
            processSuccess = { album ->
                AlbumUiModel(
                    title = album.title,
                    tracks = "${album.trackCount} tracks"
                )
            }
        )
    }
}
```

### Step 5 — Route

```kotlin
object AppRoutes {
    val albumId = longPathArgument("albumId")
    val album   = route("album", albumId)
}
```

### Step 6 — Composable Screen

```kotlin
@Composable
fun AlbumScreen(viewModel: AlbumViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(AlbumEvent.Load(id = /* navArg */ 42L))
    }

    AppBaseScreen(
        uiState = uiState,
        errorDialogConfig = ErrorDialogConfig(
            onRetry = { viewModel.onEvent(AlbumEvent.RetryClicked) }
        )
    ) { model ->
        Column {
            Text(model.title)
            Text(model.tracks)
        }
    }
}
```

### Step 7 — Navigation Graph

```kotlin
@Composable
fun AppRoot(navigationManager: NavigationManager) {
    val root = rootNode(startDestination = destinationNode(AppRoutes.album))

    CoreUiNavigator(
        navigationManager = navigationManager,
        root = root
    ) {
        destination(destinationNode(AppRoutes.album)) { entry ->
            val id = AppRoutes.album.requireArgument(entry, AppRoutes.albumId)
            AlbumScreen()
        }
    }
}
```

---

## 14. Rules and Anti-Patterns

### DO

- Always call `CoreUiStringProvider.init(context)` before creating any ViewModel.
- Use `launchUiStateUpdate` for any async data fetch. Do not manually call `setLoadingState` / `setSuccessState` for the happy path.
- Define routes using `RouteDefinition` and `RouteArgument`. Never build route strings by hand (string concatenation bypasses type safety and nullability).
- Use `handleEvent()` as the **single public entry point** for all UI interactions.
- Inject `NavigationManager` at ViewModel construction time. Never pass a `NavController` to a ViewModel.
- Use `emitEffect()` for one-shot side effects (navigation signals, toasts). Never use `MutableStateFlow` for effects.

### DO NOT

- Do not create `UIState` instances directly in Composables. They are owned by the ViewModel.
- Do not bypass `AppBaseScreen` to manually render loading/error states unless `BaseLoadingType.NONE` / `UIErrorDisplayMode.NONE` is intentionally set.
- Do not put navigation logic inside Composables. All navigation must go through `NavigationManager` → ViewModel → `NavigationCommand`.
- Do not use `CoreUiStringProvider` directly inside domain layer classes. String resolution is a presentation concern.
- Do not call `navigateToRoute()` with a raw hand-built string when a `RouteDefinition` exists — use `createRoute()`.
- Do not implement `UiErrorMapper` in `BaseViewModel` subclasses. Provide a custom mapper at construction time instead.

---

## 15. Dependency Graph

```
AppBaseScreen
  └── UIState<T>
        ├── UIStatus
        ├── UIError
        │     └── UIErrorDisplayMode
        └── [T: UI data model]

BaseViewModel<UI_TYPE, UI_EVENT, UI_EFFECT>
  ├── NavigationManager (optional)
  │     └── NavigationCommand
  ├── StringResolver
  │     └── CoreUiStringProvider (default)
  ├── UiErrorMapper
  │     └── DefaultUiErrorMapper (default)
  ├── UIState<UI_TYPE>  ← emits via StateFlow
  └── UI_EFFECT         ← emits via SharedFlow

CoreUiNavigator
  ├── NavigationManager
  ├── NavigationRootNode
  │     ├── NavigationFlowNode
  │     └── NavigationDestination
  │           └── RouteDefinition
  │                 └── RouteArgument<T>
  │                       └── RouteValueType<T>
  └── NavGraphBuilder (builder lambda using .destination() / .flow())

Resource<T>
  └── ResourceError (domain → DefaultUiErrorMapper → UIError)
```

---

*Last updated: 2026-05-13 — covers CoreUI as of commit `081485c`*
