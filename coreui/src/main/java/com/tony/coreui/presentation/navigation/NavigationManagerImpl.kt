package com.tony.coreui.presentation.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default in-memory implementation of [NavigationManager].
 *
 * This implementation is intentionally lightweight and suitable for library consumers that
 * want a ready-to-use command bus without introducing an additional navigation abstraction.
 */
class NavigationManagerImpl : NavigationManager {

    private val _navigationCommands = MutableSharedFlow<NavigationCommand>(extraBufferCapacity = 8)
    override val navigationCommands = _navigationCommands.asSharedFlow()

    private val _currentRoute = MutableStateFlow<String?>(null)
    override val currentRoute = _currentRoute.asStateFlow()

    override fun navigate(route: String, options: NavigationOptions) {
        if (route == _currentRoute.value) {
            return
        }
        _currentRoute.value = route
        _navigationCommands.tryEmit(NavigationCommand.Navigate(route, options))
    }

    override fun navigateUp() {
        _navigationCommands.tryEmit(NavigationCommand.NavigateUp)
    }

    override fun popBackStack(route: String?, inclusive: Boolean) {
        _navigationCommands.tryEmit(NavigationCommand.PopBackStack(route, inclusive))
    }

    override fun navigateAndClearBackStack(route: String, popUpToRoute: String?, inclusive: Boolean) {
        if (route == _currentRoute.value) {
            return
        }
        _currentRoute.value = route
        _navigationCommands.tryEmit(
            NavigationCommand.NavigateAndClearBackStack(
                route = route,
                popUpToRoute = popUpToRoute,
                inclusive = inclusive
            )
        )
    }

    override fun onRouteChanged(route: String?) {
        _currentRoute.value = route
    }
}