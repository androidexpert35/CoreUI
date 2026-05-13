package com.tony.coreui.presentation.navigation

/**
 * Represents a navigation intent emitted by a [NavigationManager].
 *
 * A host application should interpret these commands and map them to the navigation library
 * used by the app, such as Navigation Compose, Voyager, or a custom navigator.
 */
sealed interface NavigationCommand {
    /**
     * Requests navigation to [route] using the supplied [options].
     */
    data class Navigate(
        val route: String,
        val options: NavigationOptions = NavigationOptions()
    ) : NavigationCommand

    /**
     * Requests upward navigation.
     */
    data object NavigateUp : NavigationCommand

    /**
     * Requests popping the back stack up to [route], or a regular pop when [route] is `null`.
     */
    data class PopBackStack(
        val route: String?,
        val inclusive: Boolean
    ) : NavigationCommand

    /**
     * Requests navigation to [route] after clearing a portion of the current back stack.
     */
    data class NavigateAndClearBackStack(
        val route: String,
        val popUpToRoute: String?,
        val inclusive: Boolean
    ) : NavigationCommand
}
