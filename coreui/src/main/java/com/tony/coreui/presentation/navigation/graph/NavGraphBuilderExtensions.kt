package com.tony.coreui.presentation.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Registers [destination] as a composable in this [NavGraphBuilder].
 *
 * The destination's route pattern and navigation arguments are taken directly from its
 * [NavigationDestination.routeDefinition], so argument declarations stay in sync automatically.
 *
 * @param destination the typed destination node to register.
 * @param content the composable rendered when this destination is on the back stack.
 */
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

/**
 * Registers [flow] as a nested navigation graph in this [NavGraphBuilder].
 *
 * All destinations added inside [builder] belong to the sub-graph identified by
 * [NavigationFlowNode.route].
 *
 * @param flow the flow node that defines the sub-graph route and its start destination.
 * @param builder DSL block used to populate the nested graph with destinations and other flows.
 */
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
