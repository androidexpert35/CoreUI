package com.tony.coreui.presentation.navigation

/**
 * Represents a navigation intent emitted by a [NavigationManager].
 *
 * A host application should interpret these commands and map them to its navigation runtime.
 */
sealed interface NavigationCommand {
    /**
     * Navigates to [route] using the supplied [options].
     *
     * @param route the destination route string.
     * @param options navigation modifiers such as single-top behaviour and pop-up targets.
     */
    data class Navigate(
        val route: String,
        val options: NavigationOptions = NavigationOptions()
    ) : NavigationCommand

    /** Pops the current back-stack entry, equivalent to the device back action. */
    data object NavigateUp : NavigationCommand

    /**
     * Pops the back stack to [route], or removes only the top entry when [route] is `null`.
     *
     * @param route the destination to pop to, or `null` to pop a single entry.
     * @param inclusive when `true`, the entry matching [route] is also removed.
     */
    data class PopBackStack(
        val route: String?,
        val inclusive: Boolean
    ) : NavigationCommand

    /**
     * Navigates to [route] while clearing the back stack up to [popUpToRoute].
     *
     * @param route the destination to navigate to.
     * @param popUpToRoute the route to clear the back stack to before navigating, or `null` to
     * clear to the graph start destination.
     * @param inclusive when `true`, the entry matching [popUpToRoute] is also removed.
     */
    data class NavigateAndClearBackStack(
        val route: String,
        val popUpToRoute: String?,
        val inclusive: Boolean
    ) : NavigationCommand
}
