package com.tony.coreui.navigation

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction that decouples view models from a concrete navigator implementation.
 */
interface NavigationManager {
    val navigationCommands: SharedFlow<NavigationCommand>
    val currentRoute: StateFlow<String?>

    fun navigate(route: String, options: NavigationOptions = NavigationOptions())

    fun navigateUp()

    fun popBackStack(route: String? = null, inclusive: Boolean = false)

    fun navigateAndClearBackStack(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    )

    fun onRouteChanged(route: String?)
}
