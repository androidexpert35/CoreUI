# CoreUI

`coreui` is a reusable Android AAR module that extracts the generic presentation primitives from Audiphile Music Player and repackages them under `com.tony.coreui`.

## Architecture

The module follows a pragmatic clean architecture split:

- `com.tony.coreui.domain`
  Framework-agnostic contracts and result models shared across the module.
- `com.tony.coreui.data`
  Concrete infrastructure implementations used by the library.
- `com.tony.coreui.presentation`
  Compose components, UI state models and base view-model abstractions.

## Public API highlights

- `com.tony.coreui.presentation.viewmodel.BaseViewModel`
- `com.tony.coreui.presentation.state.UIState`, `UIStatus`, `UIError`
- `com.tony.coreui.presentation.state.UIErrorDisplayMode`
- `com.tony.coreui.domain.resource.Resource`, `ResourceError`
- `com.tony.coreui.presentation.components.basescreen.*`
- `com.tony.coreui.presentation.navigation.*`
- `com.tony.coreui.presentation.navigation.NavigationManagerImpl`
- `com.tony.coreui.data.strings.CoreUiStringProvider`, `StringResolver`, `AndroidStringResolver`
- `com.tony.coreui.presentation.error.UiErrorMapper`, `DefaultUiErrorMapper`

## Host setup

1. Depend on the module or published AAR.
2. Initialize the string provider once in your `Application`, or inject your own `StringResolver`.
3. Provide a `NavigationManager` implementation, or use `NavigationManagerImpl`.
4. Translate emitted `NavigationCommand` values inside your UI host.

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
import com.tony.coreui.presentation.navigation.NavigationCommand
import com.tony.coreui.presentation.navigation.NavigationManagerImpl

val navigationManager = NavigationManagerImpl()

LaunchedEffect(navController, navigationManager) {
    navigationManager.navigationCommands.collect { command ->
        when (command) {
            is NavigationCommand.Navigate -> {
                navController.navigate(command.route) {
                    launchSingleTop = command.options.launchSingleTop
                    restoreState = command.options.restoreState
                    command.options.popUpToRoute?.let { route ->
                        popUpTo(route) {
                            inclusive = command.options.popUpToInclusive
                        }
                    }
                }
            }

            NavigationCommand.NavigateUp -> navController.navigateUp()

            is NavigationCommand.PopBackStack -> {
                if (command.route == null) {
                    navController.popBackStack()
                } else {
                    navController.popBackStack(command.route, command.inclusive)
                }
            }

            is NavigationCommand.NavigateAndClearBackStack -> {
                navController.navigate(command.route) {
                    if (command.popUpToRoute == null) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                    } else {
                        popUpTo(command.popUpToRoute) {
                            inclusive = command.inclusive
                        }
                    }
                }
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
- `ResourceError` is open for host-defined error types when the default categories are not enough.

## Notes

- The module intentionally has no mandatory Hilt dependency.
- `ResourceError` is intentionally generic; hosts can keep using the defaults or inject a custom `UiErrorMapper` for richer domain-specific behavior.
- Navigation stays command-based so hosts can bind it to Navigation Compose or another navigator.
- `CoreUiStringProvider` should be initialized from the application process before non-composable string resolution occurs when you rely on the global default resolver.
