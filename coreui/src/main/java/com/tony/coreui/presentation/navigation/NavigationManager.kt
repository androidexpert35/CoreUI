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
    val navigationCommands: SharedFlow<NavigationCommand>

    val currentRoute: StateFlow<String?>

    fun navigate(route: String, options: NavigationOptions = NavigationOptions())

    fun navigate(
        route: RouteDefinition,
        vararg values: RouteValue<*>,
        options: NavigationOptions = NavigationOptions()
    ) {
        navigate(route.createRoute(*values), options)
    }

    fun navigate(
        destination: NavigationDestination,
        vararg values: RouteValue<*>,
        options: NavigationOptions = NavigationOptions()
    ) {
        navigate(destination.routeDefinition, *values, options = options)
    }

    fun navigateUp()

    fun popBackStack(route: String? = null, inclusive: Boolean = false)

    fun popBackStack(destination: NavigationDestination, inclusive: Boolean = false) {
        popBackStack(route = destination.route, inclusive = inclusive)
    }

    fun navigateAndClearBackStack(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    )

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

    fun onRouteChanged(route: String?)
}
