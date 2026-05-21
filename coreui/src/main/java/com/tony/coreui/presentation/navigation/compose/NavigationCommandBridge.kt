package com.tony.coreui.presentation.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tony.coreui.presentation.navigation.NavigationCommand
import com.tony.coreui.presentation.navigation.NavigationManager

/**
 * Side-effect composable that connects a [NavigationManager] to a [NavHostController].
 *
 * Two launched effects are started:
 * - one that keeps [NavigationManager.currentRoute] in sync with the back-stack entry reported
 *   by [NavHostController.currentBackStackEntryAsState];
 * - one that collects [NavigationManager.navigationCommands] and forwards each command to
 *   [handleNavigationCommand].
 *
 * Place this composable once at the top of the navigation host hierarchy; it produces no visible
 * UI output.
 *
 * @param navigationManager the manager whose command stream should be observed.
 * @param navController the controller that will execute the commands.
 */
@Composable
fun NavigationCommandBridge(
    navigationManager: NavigationManager,
    navController: NavHostController
) {
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(navigationManager, currentRoute) {
        navigationManager.onRouteChanged(currentRoute)
    }

    LaunchedEffect(navigationManager, navController) {
        navigationManager.navigationCommands.collect { command ->
            navController.handleNavigationCommand(command)
        }
    }
}

/**
 * Translates a [NavigationCommand] into the corresponding [NavHostController] call.
 *
 * @receiver the controller on which the navigation action is performed.
 * @param command the command to execute.
 * @return `true` if a forward navigation occurred; the result of [navigateUp] or [popBackStack]
 * for backwards-navigation commands.
 */
fun NavHostController.handleNavigationCommand(command: NavigationCommand): Boolean {
    return when (command) {
        is NavigationCommand.Navigate -> {
            navigate(command.route) {
                launchSingleTop = command.options.launchSingleTop
                restoreState = command.options.restoreState
                command.options.popUpToRoute?.let { route ->
                    popUpTo(route) {
                        inclusive = command.options.popUpToInclusive
                    }
                }
            }
            true
        }

        NavigationCommand.NavigateUp -> navigateUp()

        is NavigationCommand.PopBackStack -> {
            if (command.route == null) {
                popBackStack()
            } else {
                popBackStack(command.route, command.inclusive)
            }
        }

        is NavigationCommand.NavigateAndClearBackStack -> {
            navigate(command.route) {
                if (command.popUpToRoute == null) {
                    popUpTo(graph.findStartDestination().id) {
                        inclusive = command.inclusive
                    }
                } else {
                    popUpTo(command.popUpToRoute) {
                        inclusive = command.inclusive
                    }
                }
                launchSingleTop = true
            }
            true
        }
    }
}
