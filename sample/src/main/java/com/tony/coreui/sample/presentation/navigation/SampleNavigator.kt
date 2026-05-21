package com.tony.coreui.sample.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tony.coreui.presentation.navigation.compose.CoreUiNavigator
import com.tony.coreui.presentation.navigation.graph.destination
import com.tony.coreui.presentation.navigation.graph.destinationNode
import com.tony.coreui.presentation.navigation.graph.flow
import com.tony.coreui.presentation.navigation.graph.flowNode
import com.tony.coreui.presentation.navigation.graph.rootNode
import com.tony.coreui.sample.app.SampleAppContainer
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.presentation.feature.detail.DetailRoute
import com.tony.coreui.sample.presentation.feature.library.LibraryRoute

private val libraryDestination = destinationNode(SampleRoutes.library)
private val detailDestination = destinationNode(SampleRoutes.detail)
private val showcaseFlow = flowNode(
    route = "showcase",
    startDestination = libraryDestination
)
private val root = rootNode(startDestination = showcaseFlow)

/** Hosts the sample's typed navigation graph through `CoreUiNavigator`. */
@Composable
fun SampleNavigator(appContainer: SampleAppContainer) {
    CoreUiNavigator(
        navigationManager = appContainer.navigationManager,
        root = root,
        modifier = Modifier.fillMaxSize()
    ) {
        flow(showcaseFlow) {
            destination(libraryDestination) { backStackEntry ->
                LibraryRoute(
                    initialFilter = SampleRoutes.library.getArgument(
                        backStackEntry = backStackEntry,
                        argument = SampleRoutes.libraryFilter
                    ) ?: DemoFilter.ALL,
                    appContainer = appContainer
                )
            }

            destination(detailDestination) { backStackEntry ->
                DetailRoute(
                    albumId = SampleRoutes.detail.requireArgument(
                        backStackEntry = backStackEntry,
                        argument = SampleRoutes.albumId
                    ),
                    initialSection = SampleRoutes.detail.getArgument(
                        backStackEntry = backStackEntry,
                        argument = SampleRoutes.detailSection
                    ) ?: DetailSection.OVERVIEW,
                    appContainer = appContainer
                )
            }
        }
    }
}
