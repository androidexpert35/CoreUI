package com.tony.coreui.presentation.navigation

/**
 * Framework-agnostic navigation options attached to [NavigationCommand.Navigate].
 *
 * Hosts are expected to translate these flags to the semantics of their navigation solution.
 *
 * @param launchSingleTop requests single-top semantics when navigating to a destination that may
 * already be on top of the back stack.
 * @param restoreState requests restoration of previously saved destination state when supported by
 * the host navigator.
 * @param popUpToRoute optional route used as the target of a pop-up-to operation before
 * navigation.
 * @param popUpToInclusive whether [popUpToRoute], when provided, should also be removed from the
 * back stack.
 */
data class NavigationOptions(
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
    val popUpToRoute: String? = null,
    val popUpToInclusive: Boolean = false
)
