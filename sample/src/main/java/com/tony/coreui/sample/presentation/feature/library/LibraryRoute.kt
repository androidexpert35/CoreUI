package com.tony.coreui.sample.presentation.feature.library

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tony.coreui.sample.app.SampleAppContainer
import com.tony.coreui.sample.app.sampleViewModelFactory
import com.tony.coreui.sample.domain.model.DemoFilter

/**
 * Route entry point for the defaults-first sample feature.
 *
 * The navigation host passes the typed initial filter argument here and the route constructs the
 * screen's ViewModel from the shared sample container.
 */
@Composable
fun LibraryRoute(
    initialFilter: DemoFilter,
    appContainer: SampleAppContainer
) {
    val viewModel: LibraryViewModel = viewModel(
        factory = sampleViewModelFactory {
            LibraryViewModel(
                initialFilter = initialFilter,
                repository = appContainer.showcaseRepository,
                navigationManager = appContainer.navigationManager
            )
        }
    )

    LibraryScreen(
        viewModel = viewModel,
        initialFilter = initialFilter
    )
}
