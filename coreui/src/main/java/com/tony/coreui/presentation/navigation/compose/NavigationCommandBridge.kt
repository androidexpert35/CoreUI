package com.tony.coreui.presentation.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tony.coreui.presentation.navigation.NavigationCommand
import com.tony.coreui.presentation.navigation.NavigationManager

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
