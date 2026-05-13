package com.tony.coreui.presentation.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.destination(
    destination: NavigationDestination,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.route,
        arguments = destination.routeDefinition.navArguments
    ) { backStackEntry ->
        content(backStackEntry)
    }
}

fun NavGraphBuilder.flow(
    flow: NavigationFlowNode,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = flow.route,
        startDestination = flow.startDestination.route,
        builder = builder
    )
}
