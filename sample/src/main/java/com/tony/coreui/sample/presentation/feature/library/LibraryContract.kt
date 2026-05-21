package com.tony.coreui.sample.presentation.feature.library

import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.domain.model.LibraryPreviewMode

/** Full render model consumed by the defaults-first library screen. */
data class LibraryUiModel(
    val title: String,
    val subtitle: String,
    val selectedFilter: DemoFilter,
    val previewMode: LibraryPreviewMode,
    val detailSection: DetailSection,
    val albums: List<LibraryAlbumCardUiModel>
)

/** Compact card model rendered inside the library screen's scrolling content. */
data class LibraryAlbumCardUiModel(
    val id: Long,
    val title: String,
    val artist: String,
    val summary: String,
    val meta: String,
    val badge: String
)

/** User interactions supported by the library screen. */
sealed interface LibraryEvent {
    data class FilterSelected(val filter: DemoFilter) : LibraryEvent
    data class PreviewModeSelected(val previewMode: LibraryPreviewMode) : LibraryEvent
    data class DetailSectionSelected(val detailSection: DetailSection) : LibraryEvent
    data class AlbumSelected(val albumId: Long) : LibraryEvent
    data object ReloadRequested : LibraryEvent
}

/** One-off effects emitted by the library screen. */
sealed interface LibraryEffect {
    data class ShowMessage(val message: String) : LibraryEffect
}
