package com.tony.coreui.presentation.navigation.graph

import com.tony.coreui.presentation.navigation.route.RouteDefinition

interface NavigationNode {
    val route: String
}

data class NavigationDestination(
    val routeDefinition: RouteDefinition
) : NavigationNode {
    override val route: String = routeDefinition.routePattern
}

data class NavigationFlowNode(
    override val route: String,
    val startDestination: NavigationDestination
) : NavigationNode

data class NavigationRootNode(
    override val route: String = "root",
    val startDestination: NavigationNode
) : NavigationNode

fun destinationNode(routeDefinition: RouteDefinition): NavigationDestination =
    NavigationDestination(routeDefinition = routeDefinition)

fun flowNode(
    route: String,
    startDestination: NavigationDestination
): NavigationFlowNode = NavigationFlowNode(
    route = route,
    startDestination = startDestination
)

fun rootNode(
    startDestination: NavigationNode,
    route: String = "root"
): NavigationRootNode = NavigationRootNode(
    route = route,
    startDestination = startDestination
)
