# CoreUI

`coreui` is a reusable Android AAR module that extracts the generic presentation primitives from Audiphile Music Player and repackages them under `com.tony.coreui`.

## What is included

- `com.tony.coreui.viewmodel.BaseViewModel`
- `com.tony.coreui.state.UIState`, `UIStatus`, `UIError`
- `com.tony.coreui.resource.Resource`, `ResourceError`
- `com.tony.coreui.components.basescreen.*`
- `com.tony.coreui.navigation.*`
- `com.tony.coreui.strings.CoreUiStringProvider`

## Host setup

1. Depend on the module or published AAR.
2. Initialize the string provider once in your `Application`.
3. Provide a `NavigationManager` implementation, or use `NavigationManagerImpl`.
4. Translate emitted `NavigationCommand` values inside your UI host.

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreUiStringProvider.init(this)
    }
}
```

```kotlin
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
@Composable
fun ExampleScreen(viewModel: ExampleViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppBaseScreen(
        uiState = uiState,
        loadingType = BaseLoadingType.DEFAULT,
        errorDialogConfig = ErrorDialogConfig(
            onConfirm = { viewModel.showErrorPopup(false) },
            onDismissRequest = { viewModel.showErrorPopup(false) }
        )
    ) { data ->
        ExampleContent(data = data)
    }
}
```

## Notes

- The module intentionally has no mandatory Hilt dependency.
- `ResourceError` is generic, but hosts with richer domain errors should map them before they hit `BaseViewModel`.
- Navigation stays command-based so hosts can bind it to Navigation Compose or another navigator.
