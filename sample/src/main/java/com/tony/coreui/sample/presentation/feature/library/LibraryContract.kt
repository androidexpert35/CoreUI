package com.tony.coreui.sample.presentation.feature.library

import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.domain.model.LibraryPreviewMode

data class LibraryUiModel(
    val title: String,
    val subtitle: String,
    val selectedFilter: DemoFilter,
    val previewMode: LibraryPreviewMode,
    val detailSection: DetailSection,
    val albums: List<LibraryAlbumCardUiModel>
)

data class LibraryAlbumCardUiModel(
    val id: Long,
    val title: String,
    val artist: String,
    val summary: String,
    val meta: String,
    val badge: String
)

sealed interface LibraryEvent {
    data class FilterSelected(val filter: DemoFilter) : LibraryEvent
    data class PreviewModeSelected(val previewMode: LibraryPreviewMode) : LibraryEvent
    data class DetailSectionSelected(val detailSection: DetailSection) : LibraryEvent
    data class AlbumSelected(val albumId: Long) : LibraryEvent
    data object ReloadRequested : LibraryEvent
}

sealed interface LibraryEffect {
    data class ShowMessage(val message: String) : LibraryEffect
}
