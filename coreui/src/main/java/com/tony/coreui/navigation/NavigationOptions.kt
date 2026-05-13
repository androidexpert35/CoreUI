package com.tony.coreui.navigation

/**
 * Navigation flags that a host can translate to its own navigation framework.
 */
data class NavigationOptions(
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
    val popUpToRoute: String? = null,
    val popUpToInclusive: Boolean = false
)
