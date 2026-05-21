# CoreUI

`coreui` is a reusable Android AAR module that extracts the generic presentation primitives from Audiphile Music Player and repackages them under `com.tony.coreui`.

## Architecture

The module follows a pragmatic clean architecture split:

- `com.tony.coreui.domain`
  Framework-agnostic contracts and result models shared across the module.
- `com.tony.coreui.data`
  Concrete infrastructure implementations used by the library, including the default
  `NavigationManagerImpl`.
- `com.tony.coreui.presentation`
  Compose components, UI state models, base view-model abstractions, and the navigation
  contracts, route definitions, graph helpers, and Compose bridge.

## Public API highlights

- `com.tony.coreui.presentation.viewmodel.BaseViewModel`
- `com.tony.coreui.presentation.state.UIState`, `UIStatus`, `UIError`
- `com.tony.coreui.presentation.state.UIErrorDisplayMode`
- `com.tony.coreui.domain.resource.Resource`, `ResourceError`
- `com.tony.coreui.presentation.components.basescreen.*`
- `com.tony.coreui.presentation.navigation.*`
- `com.tony.coreui.presentation.navigation.route.*`
- `com.tony.coreui.presentation.navigation.graph.*`
- `com.tony.coreui.presentation.navigation.compose.*`
- `com.tony.coreui.data.navigation.NavigationManagerImpl`
- `com.tony.coreui.data.strings.CoreUiStringProvider`, `StringResolver`, `AndroidStringResolver`
- `com.tony.coreui.presentation.error.UiErrorMapper`, `DefaultUiErrorMapper`

## Host setup

1. Depend on the module or published AAR.
2. Initialize the string provider once in your `Application`, or inject your own `StringResolver`.
3. Choose whether to use the built-in navigation stack.
4. If you do, provide a `NavigationManager` implementation, define route and graph nodes, and host
   them with `CoreUiNavigator`.

```kotlin
import com.tony.coreui.data.strings.CoreUiStringProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreUiStringProvider.init(this)
    }
}
```

If you prefer avoiding global initialization, inject a resolver directly into your view models:

```kotlin
import com.tony.coreui.data.strings.AndroidStringResolver
import com.tony.coreui.presentation.error.DefaultUiErrorMapper
import com.tony.coreui.presentation.viewmodel.BaseViewModel

class ExampleViewModel(
    context: Context
) : BaseViewModel<ExampleUiState, ExampleEvent, ExampleEffect>(
    stringResolver = AndroidStringResolver(context),
    uiErrorMapper = DefaultUiErrorMapper(AndroidStringResolver(context))
) {
    override fun handleEvent(event: ExampleEvent) = Unit
}
```

```kotlin
import com.tony.coreui.data.navigation.NavigationManagerImpl
import com.tony.coreui.presentation.navigation.compose.CoreUiNavigator
import com.tony.coreui.presentation.navigation.graph.destination
import com.tony.coreui.presentation.navigation.graph.destinationNode
import com.tony.coreui.presentation.navigation.graph.flow
import com.tony.coreui.presentation.navigation.graph.flowNode
import com.tony.coreui.presentation.navigation.graph.rootNode
import com.tony.coreui.presentation.navigation.route.longPathArgument
import com.tony.coreui.presentation.navigation.route.route
import com.tony.coreui.presentation.navigation.route.with

val albumId = longPathArgument("albumId")
val homeRoute = route("home")
val albumRoute = route("album", albumId)

val homeDestination = destinationNode(homeRoute)
val albumDestination = destinationNode(albumRoute)
val mainFlow = flowNode(route = "main", startDestination = homeDestination)
val root = rootNode(startDestination = mainFlow)
val navigationManager = NavigationManagerImpl()

@Composable
fun AppNavigator() {
    CoreUiNavigator(
        navigationManager = navigationManager,
        root = root
    ) {
        flow(mainFlow) {
            destination(homeDestination) {
                HomeScreen(
                    onAlbumClick = { id ->
                        navigationManager.navigate(albumRoute, albumId with id)
                    }
                )
            }
            destination(albumDestination) { entry ->
                AlbumScreen(
                    albumId = albumRoute.requireArgument(entry, albumId)
                )
            }
        }
    }
}
```

```kotlin
import com.tony.coreui.presentation.components.basescreen.AppBaseScreen
import com.tony.coreui.presentation.components.basescreen.BaseLoadingType
import com.tony.coreui.presentation.components.basescreen.ErrorDialogConfig

@Composable
fun ExampleScreen(viewModel: ExampleViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppBaseScreen(
        uiState = uiState,
        loadingType = BaseLoadingType.DEFAULT,
        onErrorDialogDismiss = viewModel::dismissErrorPopup,
        errorDialogConfig = ErrorDialogConfig()
    ) { data ->
        ExampleContent(data = data)
    }
}
```

## Extensibility defaults

The library keeps its ready-to-use defaults, but the main decision points are now open:

- `BaseViewModel` accepts injectable `StringResolver` and `UiErrorMapper` strategies.
- `UIError.displayMode` can switch between dialog, full-screen, or host-managed error rendering.
- `AppBaseScreen` supports `BaseScreenRenderPolicy`, `emptyContent`, `errorDialog`, and
  `contentWithState` without losing the built-in defaults.
- `NavigationOptions` supports `allowRepeatOnSameRoute` and host-defined `extras`.
- The navigation subsystem includes typed route helpers, nested flow nodes, and a default Compose
  host, but it is entirely optional.
- `ResourceError` is open for host-defined error types when the default categories are not enough.

## Notes

- The module intentionally has no mandatory Hilt dependency.
- `ResourceError` is intentionally generic; hosts can keep using the defaults or inject a custom `UiErrorMapper` for richer domain-specific behavior.
- Navigation stays command-based so hosts can use the provided Compose bridge or replace the whole
  stack with their own approach.
- `CoreUiStringProvider` should be initialized from the application process before non-composable string resolution occurs when you rely on the global default resolver.

## Example app

For a runnable end-to-end example, inspect the `sample` module in the repository. It mirrors the
same clean-architecture split as the library and demonstrates:

- a defaults-heavy screen built with `BaseViewModel` + `AppBaseScreen`
- typed route arguments hosted by `CoreUiNavigator`
- a second screen that customizes loading and error rendering without abandoning the library
  primitives
