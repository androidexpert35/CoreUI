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
- `com.tony.coreui.domain.resource.Resource`, `ResourceError`
- `com.tony.coreui.presentation.components.basescreen.*`
- `com.tony.coreui.presentation.navigation.*`
- `com.tony.coreui.presentation.navigation.NavigationManagerImpl`
- `com.tony.coreui.data.strings.CoreUiStringProvider`

## Host setup

1. Depend on the module or published AAR.
2. Initialize the string provider once in your `Application`.
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

## Notes

- The module intentionally has no mandatory Hilt dependency.
- `ResourceError` is intentionally generic; hosts with richer domain errors should map them before they hit `BaseViewModel`.
- Navigation stays command-based so hosts can bind it to Navigation Compose or another navigator.
- `CoreUiStringProvider` should be initialized from the application process before non-composable string resolution occurs.
