package com.tony.coreui.presentation.navigation

/**
 * Framework-agnostic navigation options attached to [NavigationCommand.Navigate].
 *
 * Hosts are expected to translate these flags to the semantics of their navigation solution.
 */
data class NavigationOptions(
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
    val popUpToRoute: String? = null,
    val popUpToInclusive: Boolean = false
)
