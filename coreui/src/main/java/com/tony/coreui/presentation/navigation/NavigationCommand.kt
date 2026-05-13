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
     *
     * @param route logical destination identifier understood by the host navigator.
     * @param options navigation options that refine how the destination should be opened.
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
     *
     * @param route optional route that acts as the pop target.
     * @param inclusive whether [route], when provided, should also be removed from the back stack.
     */
    data class PopBackStack(
        val route: String?,
        val inclusive: Boolean
    ) : NavigationCommand

    /**
     * Requests navigation to [route] after clearing a portion of the current back stack.
     *
     * @param route logical destination identifier understood by the host navigator.
     * @param popUpToRoute optional route used as the boundary for the back stack clearing
     * operation.
     * @param inclusive whether [popUpToRoute], when provided, should also be removed.
     */
    data class NavigateAndClearBackStack(
        val route: String,
        val popUpToRoute: String?,
        val inclusive: Boolean
    ) : NavigationCommand
}
