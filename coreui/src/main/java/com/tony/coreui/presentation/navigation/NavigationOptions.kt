package com.tony.coreui.presentation.navigation

/**
 * Framework-agnostic navigation options attached to [NavigationCommand.Navigate].
 */
data class NavigationOptions(
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
    val popUpToRoute: String? = null,
    val popUpToInclusive: Boolean = false,
    val allowRepeatOnSameRoute: Boolean = false,
    val extras: Map<String, Any?> = emptyMap()
)
