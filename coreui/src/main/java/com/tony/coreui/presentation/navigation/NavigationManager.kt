package com.tony.coreui.presentation.navigation

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for dispatching navigation intents without coupling callers to a specific
 * navigation framework.
 *
 * View models and domain-facing presentation logic should depend on this abstraction, while
 * host applications remain responsible for observing [navigationCommands] and translating each
 * emitted [NavigationCommand] into framework-specific navigation calls.
 */
interface NavigationManager {
    val navigationCommands: SharedFlow<NavigationCommand>
    val currentRoute: StateFlow<String?>

    /**
     * Requests navigation to [route].
     *
     * Implementations may ignore duplicate requests when [route] already matches [currentRoute].
     */
    fun navigate(route: String, options: NavigationOptions = NavigationOptions())

    /**
     * Requests an upward navigation event.
     */
    fun navigateUp()

    /**
     * Requests a back stack pop operation.
     *
     * When [route] is `null`, the host should perform a regular back navigation.
     */
    fun popBackStack(route: String? = null, inclusive: Boolean = false)

    /**
     * Requests navigation to [route] while clearing part of the existing back stack.
     */
    fun navigateAndClearBackStack(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    )

    /**
     * Synchronizes the currently visible route with the navigation host state.
     */
    fun onRouteChanged(route: String?)
}
