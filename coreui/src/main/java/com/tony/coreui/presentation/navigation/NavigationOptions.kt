package com.tony.coreui.presentation.navigation

/**
 * Framework-agnostic navigation options attached to [NavigationCommand.Navigate].
 *
 * @param launchSingleTop when `true`, avoids creating a duplicate entry if the destination is
 * already at the top of the back stack.
 * @param restoreState when `true`, restores previously saved state when re-navigating to a
 * destination that was previously on the back stack.
 * @param popUpToRoute route to clear the back stack to before navigating. No pop-up occurs when
 * `null`.
 * @param popUpToInclusive when `true`, the entry matching [popUpToRoute] is also removed.
 * @param allowRepeatOnSameRoute when `true`, allows navigating to the same route as the current
 * destination. Defaults to `false` to prevent duplicate back-stack entries.
 * @param extras optional opaque payload forwarded alongside the navigation command for
 * host-specific extensions.
 */
data class NavigationOptions(
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
    val popUpToRoute: String? = null,
    val popUpToInclusive: Boolean = false,
    val allowRepeatOnSameRoute: Boolean = false,
    val extras: Map<String, Any?> = emptyMap()
)
