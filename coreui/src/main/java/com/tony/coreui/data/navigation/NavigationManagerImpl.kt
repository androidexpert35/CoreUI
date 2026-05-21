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

    /**
     * Navigates to [route] unless it equals the current route and
     * [NavigationOptions.allowRepeatOnSameRoute] is `false`.
     *
     * @param route the destination route string.
     * @param options navigation modifiers controlling single-top and pop-up behaviour.
     */
    override fun navigate(route: String, options: NavigationOptions) {
        if (route == _currentRoute.value && !options.allowRepeatOnSameRoute) {
            return
        }

        _currentRoute.value = route
        _navigationCommands.tryEmit(NavigationCommand.Navigate(route, options))
    }

    /** Emits a [NavigationCommand.NavigateUp] command to pop the current back-stack entry. */
    override fun navigateUp() {
        _navigationCommands.tryEmit(NavigationCommand.NavigateUp)
    }

    /**
     * Emits a [NavigationCommand.PopBackStack] command.
     *
     * @param route the destination to pop to, or `null` to pop only the top entry.
     * @param inclusive when `true`, the entry matching [route] is also removed.
     */
    override fun popBackStack(route: String?, inclusive: Boolean) {
        _navigationCommands.tryEmit(NavigationCommand.PopBackStack(route, inclusive))
    }

    /**
     * Navigates to [route] and clears the back stack, unless [route] is already the current route.
     *
     * @param route the destination to navigate to.
     * @param popUpToRoute the route to pop the back stack to before navigating, or `null` to clear
     * to the graph start destination.
     * @param inclusive when `true`, the entry matching [popUpToRoute] is also removed.
     */
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

    /**
     * Updates [currentRoute] to reflect a back-stack change observed by the navigation host.
     *
     * @param route the route of the now-current back-stack entry, or `null` when the stack is empty.
     */
    override fun onRouteChanged(route: String?) {
        _currentRoute.value = route
    }
}
