package com.tony.coreui.presentation.navigation

import com.tony.coreui.presentation.navigation.graph.NavigationDestination
import com.tony.coreui.presentation.navigation.route.RouteDefinition
import com.tony.coreui.presentation.navigation.route.RouteValue
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for dispatching navigation intents without coupling callers to `NavController`.
 */
interface NavigationManager {
    /** Hot stream of navigation commands that the navigation host should execute in order. */
    val navigationCommands: SharedFlow<NavigationCommand>

    /** The route of the current back-stack entry, or `null` when the stack is empty. */
    val currentRoute: StateFlow<String?>

    /**
     * Navigates to [route] with the given [options].
     *
     * @param route the destination route string.
     * @param options navigation modifiers.
     */
    fun navigate(route: String, options: NavigationOptions = NavigationOptions())

    /**
     * Navigates to the destination described by [route], resolving argument values from [values].
     *
     * @param route the [RouteDefinition] of the target destination.
     * @param values type-safe argument values to embed in the route string.
     * @param options navigation modifiers.
     */
    fun navigate(
        route: RouteDefinition,
        vararg values: RouteValue<*>,
        options: NavigationOptions = NavigationOptions()
    ) {
        navigate(route.createRoute(*values), options)
    }

    /**
     * Navigates to [destination], resolving argument values from [values].
     *
     * @param destination the typed destination node.
     * @param values type-safe argument values to embed in the route string.
     * @param options navigation modifiers.
     */
    fun navigate(
        destination: NavigationDestination,
        vararg values: RouteValue<*>,
        options: NavigationOptions = NavigationOptions()
    ) {
        navigate(destination.routeDefinition, *values, options = options)
    }

    /** Pops the current back-stack entry. */
    fun navigateUp()

    /**
     * Pops the back stack to [route], or removes only the top entry when [route] is `null`.
     *
     * @param route the destination to pop to, or `null` to pop a single entry.
     * @param inclusive when `true`, the entry matching [route] is also removed.
     */
    fun popBackStack(route: String? = null, inclusive: Boolean = false)

    /**
     * Pops the back stack to [destination].
     *
     * @param destination the destination to pop to.
     * @param inclusive when `true`, the entry for [destination] is also removed.
     */
    fun popBackStack(destination: NavigationDestination, inclusive: Boolean = false) {
        popBackStack(route = destination.route, inclusive = inclusive)
    }

    /**
     * Navigates to [route] while clearing the back stack.
     *
     * @param route the destination route string.
     * @param popUpToRoute the route to pop the back stack to, or `null` to clear to the graph
     * start destination.
     * @param inclusive when `true`, the entry matching [popUpToRoute] is also removed.
     */
    fun navigateAndClearBackStack(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    )

    /**
     * Navigates to [route] and clears the back stack, resolving argument values from [values].
     *
     * @param route the [RouteDefinition] of the target destination.
     * @param values type-safe argument values to embed in the route string.
     * @param popUpToRoute the route to pop to before navigating.
     * @param inclusive when `true`, the entry matching [popUpToRoute] is also removed.
     */
    fun navigateAndClearBackStack(
        route: RouteDefinition,
        vararg values: RouteValue<*>,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    ) {
        navigateAndClearBackStack(
            route = route.createRoute(*values),
            popUpToRoute = popUpToRoute,
            inclusive = inclusive
        )
    }

    /**
     * Notifies the manager that the active back-stack entry has changed.
     *
     * Call this from a navigation-tracking side effect (e.g., [NavigationCommandBridge]) to keep
     * [currentRoute] consistent with the actual back-stack state.
     *
     * @param route the route of the current back-stack entry, or `null` when the stack is empty.
     */
    fun onRouteChanged(route: String?)
}
