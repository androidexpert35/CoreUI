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
    /**
     * Hot stream of one-off navigation commands emitted by this manager.
     */
    val navigationCommands: SharedFlow<NavigationCommand>

    /**
     * Observable route considered current by this manager.
     */
    val currentRoute: StateFlow<String?>

    /**
     * Requests navigation to [route].
     *
     * Implementations may ignore duplicate requests when [route] already matches [currentRoute].
     *
     * @param route logical destination identifier understood by the host navigator.
     * @param options framework-agnostic navigation options associated with this request.
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
     *
     * @param route optional route that acts as the pop target.
     * @param inclusive whether [route], when provided, should also be removed.
     */
    fun popBackStack(route: String? = null, inclusive: Boolean = false)

    /**
     * Requests navigation to [route] while clearing part of the existing back stack.
     *
     * @param route logical destination identifier understood by the host navigator.
     * @param popUpToRoute optional route used as the boundary for the clear-back-stack operation.
     * @param inclusive whether [popUpToRoute], when provided, should also be removed.
     */
    fun navigateAndClearBackStack(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    )

    /**
     * Synchronizes the currently visible route with the navigation host state.
     *
     * @param route route currently visible to the user, or `null` when unknown.
     */
    fun onRouteChanged(route: String?)
}
