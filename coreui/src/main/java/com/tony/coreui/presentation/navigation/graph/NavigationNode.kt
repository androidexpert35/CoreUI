package com.tony.coreui.presentation.navigation.graph

import com.tony.coreui.presentation.navigation.route.RouteDefinition

/**
 * Marker interface for any node in the navigation graph.
 *
 * Each node is uniquely identified by its [route] string, which the navigation runtime uses to
 * address the corresponding destination or sub-graph.
 */
interface NavigationNode {
    /** Unique route string that identifies this node within the navigation graph. */
    val route: String
}

/**
 * A leaf node in the navigation graph that maps directly to a single screen.
 *
 * The resolved [route] is derived from [routeDefinition]'s [RouteDefinition.routePattern],
 * ensuring that registered navigation arguments are always consistent with the destination.
 *
 * @param routeDefinition the type-safe route definition backing this destination.
 */
data class NavigationDestination(
    val routeDefinition: RouteDefinition
) : NavigationNode {
    override val route: String = routeDefinition.routePattern
}

/**
 * A nested navigation graph node that groups a set of related destinations behind a shared [route].
 *
 * @param route the route string that identifies this sub-graph in the navigation back stack.
 * @param startDestination the first destination displayed when the flow is entered.
 */
data class NavigationFlowNode(
    override val route: String,
    val startDestination: NavigationDestination
) : NavigationNode

/**
 * The top-level navigation graph node that anchors the entire [NavHost].
 *
 * @param route the route string for the root graph. Defaults to `"root"`.
 * @param startDestination the first [NavigationNode] shown when the host is composed.
 */
data class NavigationRootNode(
    override val route: String = "root",
    val startDestination: NavigationNode
) : NavigationNode

/**
 * Creates a [NavigationDestination] backed by [routeDefinition].
 *
 * @param routeDefinition the route definition that describes the destination's route pattern and
 * argument list.
 */
fun destinationNode(routeDefinition: RouteDefinition): NavigationDestination =
    NavigationDestination(routeDefinition = routeDefinition)

/**
 * Creates a [NavigationFlowNode] that groups destinations under [route].
 *
 * @param route the route string identifying the nested navigation graph.
 * @param startDestination the destination that is shown first when the flow is entered.
 */
fun flowNode(
    route: String,
    startDestination: NavigationDestination
): NavigationFlowNode = NavigationFlowNode(
    route = route,
    startDestination = startDestination
)

/**
 * Creates a [NavigationRootNode] that serves as the anchor for the top-level [NavHost].
 *
 * @param startDestination the first [NavigationNode] displayed when the host is composed.
 * @param route the route string for the root graph. Defaults to `"root"`.
 */
fun rootNode(
    startDestination: NavigationNode,
    route: String = "root"
): NavigationRootNode = NavigationRootNode(
    route = route,
    startDestination = startDestination
)
