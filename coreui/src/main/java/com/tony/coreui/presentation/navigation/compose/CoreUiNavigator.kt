package com.tony.coreui.presentation.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.presentation.navigation.graph.NavigationRootNode

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
