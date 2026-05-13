package com.tony.coreui.navigation

/**
 * Command-based navigation contract that a UI host can observe and execute.
 */
sealed interface NavigationCommand {
    data class Navigate(
        val route: String,
        val options: NavigationOptions = NavigationOptions()
    ) : NavigationCommand

    data object NavigateUp : NavigationCommand

    data class PopBackStack(
        val route: String?,
        val inclusive: Boolean
    ) : NavigationCommand

    data class NavigateAndClearBackStack(
        val route: String,
        val popUpToRoute: String?,
        val inclusive: Boolean
    ) : NavigationCommand
}
