package com.tony.coreui.sample.presentation.feature.detail

import com.tony.coreui.presentation.error.UiErrorMapper
import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.presentation.navigation.route.with
import com.tony.coreui.presentation.viewmodel.BaseViewModel
import com.tony.coreui.sample.R
import com.tony.coreui.sample.domain.model.DemoAlbumDetail
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailLoadMode
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.domain.repository.ShowcaseRepository
import com.tony.coreui.sample.presentation.navigation.SampleRoutes

/**
 * ViewModel for the detail screen that demonstrates more advanced CoreUI customization.
 *
 * It still uses `BaseViewModel`, but it also preserves content across some failures and relies on
 * a custom `UiErrorMapper` to change how specific error families are presented.
 */
class DetailViewModel(
    private val albumId: Long,
    private val repository: ShowcaseRepository,
    navigationManager: NavigationManager,
    uiErrorMapper: UiErrorMapper
) : BaseViewModel<DetailUiModel, DetailEvent, DetailEffect>(
    navigationManager = navigationManager,
    uiErrorMapper = uiErrorMapper
) {

    init {
        load(mode = DetailLoadMode.INITIAL)
    }

    /** Handles refreshes, failure demos, and navigation for the detail screen. */
    override fun handleEvent(event: DetailEvent) {
        when (event) {
            DetailEvent.RefreshRequested -> load(
                mode = DetailLoadMode.REFRESH,
                preserveCurrentContentOnError = true
            )

            DetailEvent.ServiceFailureRequested -> load(
                mode = DetailLoadMode.SERVICE_FAILURE,
                preserveCurrentContentOnError = true
            )

            DetailEvent.HostWarningRequested -> load(
                mode = DetailLoadMode.HOST_HANDLED_WARNING,
                preserveCurrentContentOnError = true,
                skipLoading = true
            )

            DetailEvent.ResetToDefaultsRequested -> {
                navigateAndClearBackstackTo(
                    route = SampleRoutes.library.createRoute(
                        SampleRoutes.libraryFilter with DemoFilter.ALL
                    ),
                    popUpToRoute = SampleRoutes.library.routePattern,
                    inclusive = true
                )
            }

            DetailEvent.NavigateUpRequested -> navigateUp()
            DetailEvent.RetryInitialLoadRequested -> load(mode = DetailLoadMode.INITIAL)
            DetailEvent.RelatedAlbumRequested -> uiState.value.data?.relatedAlbumId?.let { relatedId ->
                navigateToRoute(
                    SampleRoutes.detail.createRoute(
                        SampleRoutes.albumId with relatedId,
                        SampleRoutes.detailSection with DetailSection.OVERVIEW
                    )
                )
            }
        }
    }

    /**
     * Loads or refreshes the detail payload.
     *
     * The `preserveCurrentContentOnError` switch is the key customization hook in this sample:
     * it keeps the last successful content visible while still recording the new failure state.
     */
    private fun load(
        mode: DetailLoadMode,
        preserveCurrentContentOnError: Boolean = false,
        skipLoading: Boolean = false
    ) {
        launchUiStateUpdate(
            retryAction = {
                load(
                    mode = mode,
                    preserveCurrentContentOnError = preserveCurrentContentOnError,
                    skipLoading = skipLoading
                )
            },
            dataFetchBlock = { repository.getAlbumDetail(albumId = albumId, mode = mode) },
            processSuccess = { detail ->
                if (mode == DetailLoadMode.REFRESH) {
                    emitEffect(
                        DetailEffect.ShowMessage(
                            resolveString(R.string.sample_detail_snackbar_refreshed)
                        )
                    )
                }
                toDetailUiModel(detail)
            },
            updateUiAfterError = if (preserveCurrentContentOnError) {
                { uiState.value.data }
            } else {
                null
            },
            skipLoading = skipLoading
        )
    }

    /** Maps the domain model into the Compose-facing UI model consumed by the screen. */
    private fun toDetailUiModel(detail: DemoAlbumDetail): DetailUiModel = DetailUiModel(
        id = detail.id,
        title = detail.title,
        artist = detail.artist,
        summary = detail.summary,
        statsLine = "${detail.trackCount} tracks | ${detail.artist}",
        lastUpdatedLabel = detail.lastUpdatedLabel,
        defaultsHighlights = detail.defaultsHighlights,
        customizationHighlights = detail.customizationHighlights,
        relatedAlbumId = detail.relatedAlbumId,
        relatedAlbumTitle = detail.relatedAlbumTitle
    )
}
