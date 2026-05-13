package com.tony.coreui.data.navigation

import com.tony.coreui.presentation.navigation.NavigationCommand
import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.presentation.navigation.NavigationOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default in-memory implementation of [NavigationManager].
 *
 * Navigation is modeled as a presentation concern in CoreUI, while this concrete runtime
 * implementation lives in the data layer to keep the clean-architecture split honest.
 */
class NavigationManagerImpl : NavigationManager {

    private val _navigationCommands = MutableSharedFlow<NavigationCommand>(extraBufferCapacity = 8)
    override val navigationCommands = _navigationCommands.asSharedFlow()

    private val _currentRoute = MutableStateFlow<String?>(null)
    override val currentRoute = _currentRoute.asStateFlow()

    override fun navigate(route: String, options: NavigationOptions) {
        if (route == _currentRoute.value && !options.allowRepeatOnSameRoute) {
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
