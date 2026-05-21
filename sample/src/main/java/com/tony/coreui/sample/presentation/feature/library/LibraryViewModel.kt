package com.tony.coreui.sample.presentation.feature.library

import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.presentation.navigation.route.with
import com.tony.coreui.presentation.viewmodel.BaseViewModel
import com.tony.coreui.sample.R
import com.tony.coreui.sample.domain.model.DemoAlbum
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.domain.model.LibraryPreviewMode
import com.tony.coreui.sample.domain.repository.ShowcaseRepository
import com.tony.coreui.sample.presentation.navigation.SampleRoutes

class LibraryViewModel(
    initialFilter: DemoFilter,
    private val repository: ShowcaseRepository,
    navigationManager: NavigationManager
) : BaseViewModel<LibraryUiModel, LibraryEvent, LibraryEffect>(
    navigationManager = navigationManager
) {

    private var selectedFilter: DemoFilter = initialFilter
    private var previewMode: LibraryPreviewMode = LibraryPreviewMode.READY
    private var detailSection: DetailSection = DetailSection.OVERVIEW

    init {
        loadAlbums()
    }

    override fun handleEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.FilterSelected -> {
                if (selectedFilter == event.filter) return
                selectedFilter = event.filter
                emitEffect(LibraryEffect.ShowMessage(resolveString(R.string.sample_library_snackbar_filter_changed, filterLabel(event.filter))))
                loadAlbums()
            }

            is LibraryEvent.PreviewModeSelected -> {
                if (previewMode == event.previewMode) return
                previewMode = event.previewMode
                emitEffect(LibraryEffect.ShowMessage(resolveString(R.string.sample_library_snackbar_preview_changed, previewLabel(event.previewMode))))
                loadAlbums()
            }

            is LibraryEvent.DetailSectionSelected -> {
                if (detailSection == event.detailSection) return
                detailSection = event.detailSection
                updateUiState { currentState ->
                    currentState.copy(
                        data = currentState.data?.copy(detailSection = detailSection)
                    )
                }
                emitEffect(LibraryEffect.ShowMessage(resolveString(R.string.sample_library_snackbar_section_changed, sectionLabel(event.detailSection))))
            }

            is LibraryEvent.AlbumSelected -> {
                navigateToRoute(
                    SampleRoutes.detail.createRoute(
                        SampleRoutes.albumId with event.albumId,
                        SampleRoutes.detailSection with detailSection
                    )
                )
            }

            LibraryEvent.ReloadRequested -> loadAlbums()
        }
    }

    private fun loadAlbums() {
        launchUiStateUpdate(
            retryAction = ::loadAlbums,
            dataFetchBlock = {
                repository.getAlbums(
                    filter = selectedFilter,
                    previewMode = previewMode
                )
            },
            processSuccess = { albums ->
                LibraryUiModel(
                    title = resolveString(R.string.sample_library_title),
                    subtitle = resolveString(R.string.sample_library_subtitle),
                    selectedFilter = selectedFilter,
                    previewMode = previewMode,
                    detailSection = detailSection,
                    albums = albums.map(::toAlbumCardUiModel)
                )
            }
        )
    }

    private fun toAlbumCardUiModel(album: DemoAlbum): LibraryAlbumCardUiModel {
        val qualifiers = buildList {
            add("${album.trackCount} tracks")
            if (album.focusReady) add("focus-ready")
            if (album.downloaded) add("cached")
        }.joinToString("  •  ")

        return LibraryAlbumCardUiModel(
            id = album.id,
            title = album.title,
            artist = album.artist,
            summary = album.summary,
            meta = qualifiers,
            badge = album.badge
        )
    }

    private fun filterLabel(filter: DemoFilter): String = when (filter) {
        DemoFilter.ALL -> resolveString(R.string.sample_library_filter_all)
        DemoFilter.FOCUS_READY -> resolveString(R.string.sample_library_filter_focus)
        DemoFilter.DOWNLOADED -> resolveString(R.string.sample_library_filter_downloaded)
    }

    private fun previewLabel(previewMode: LibraryPreviewMode): String = when (previewMode) {
        LibraryPreviewMode.READY -> resolveString(R.string.sample_library_preview_ready)
        LibraryPreviewMode.EMPTY -> resolveString(R.string.sample_library_preview_empty)
        LibraryPreviewMode.NETWORK_DIALOG -> resolveString(R.string.sample_library_preview_network)
        LibraryPreviewMode.VALIDATION_DIALOG -> resolveString(R.string.sample_library_preview_validation)
    }

    private fun sectionLabel(section: DetailSection): String = when (section) {
        DetailSection.OVERVIEW -> resolveString(R.string.sample_detail_overview_tab)
        DetailSection.CUSTOMIZATION -> resolveString(R.string.sample_detail_customization_tab)
    }
}
