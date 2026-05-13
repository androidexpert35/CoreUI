package com.tony.coreui.presentation.navigation

/**
 * Represents a navigation intent emitted by a [NavigationManager].
 *
 * A host application should interpret these commands and map them to its navigation runtime.
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
