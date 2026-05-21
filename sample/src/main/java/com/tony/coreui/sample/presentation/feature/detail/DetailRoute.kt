package com.tony.coreui.sample.presentation.feature.detail

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tony.coreui.sample.app.SampleAppContainer
import com.tony.coreui.sample.app.sampleViewModelFactory
import com.tony.coreui.sample.domain.model.DetailSection

/**
 * Route entry point for the customization-heavy detail feature.
 *
 * This route receives the typed navigation arguments and constructs the ViewModel with the custom
 * error mapper that powers the more advanced `AppBaseScreen` behaviors in the sample.
 */
@Composable
fun DetailRoute(
    albumId: Long,
    initialSection: DetailSection,
    appContainer: SampleAppContainer
) {
    val viewModel: DetailViewModel = viewModel(
        factory = sampleViewModelFactory {
            DetailViewModel(
                albumId = albumId,
                repository = appContainer.showcaseRepository,
                navigationManager = appContainer.navigationManager,
                uiErrorMapper = appContainer.detailUiErrorMapper
            )
        }
    )

    DetailScreen(
        viewModel = viewModel,
        albumId = albumId,
        initialSection = initialSection
    )
}
