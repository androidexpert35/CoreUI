package com.tony.coreui.presentation.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.presentation.navigation.graph.NavigationRootNode

/**
 * Top-level composable that wires a [NavigationManager] to a Compose [NavHost].
 *
 * Installs a [NavigationCommandBridge] that translates [NavigationManager.navigationCommands]
 * emissions into [NavHostController] calls, then delegates graph composition to [builder].
 *
 * @param navigationManager the shared manager used to dispatch navigation commands.
 * @param root the root navigation node that defines the graph route and its start destination.
 * @param modifier [Modifier] applied to the underlying [NavHost].
 * @param navController the controller backing this navigator. Defaults to a newly remembered
 * controller; override only when the caller needs shared access to the same instance.
 * @param builder DSL block for declaring the graph's destinations and nested flows.
 */
@Composable
fun CoreUiNavigator(
    navigationManager: NavigationManager,
    root: NavigationRootNode,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    builder: NavGraphBuilder.() -> Unit
) {
    NavigationCommandBridge(
        navigationManager = navigationManager,
        navController = navController
    )

    NavHost(
        navController = navController,
        startDestination = root.startDestination.route,
        route = root.route,
        modifier = modifier,
        builder = builder
    )
}
