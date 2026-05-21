package com.tony.coreui.sample.presentation.feature.detail

/** Full render model consumed by the detail screen. */
data class DetailUiModel(
    val id: Long,
    val title: String,
    val artist: String,
    val summary: String,
    val statsLine: String,
    val lastUpdatedLabel: String,
    val defaultsHighlights: List<String>,
    val customizationHighlights: List<String>,
    val relatedAlbumId: Long?,
    val relatedAlbumTitle: String?
)

/** User interactions supported by the detail screen. */
sealed interface DetailEvent {
    data object RefreshRequested : DetailEvent
    data object ServiceFailureRequested : DetailEvent
    data object HostWarningRequested : DetailEvent
    data object ResetToDefaultsRequested : DetailEvent
    data object NavigateUpRequested : DetailEvent
    data object RetryInitialLoadRequested : DetailEvent
    data object RelatedAlbumRequested : DetailEvent
}

/** One-off effects emitted by the detail screen. */
sealed interface DetailEffect {
    data class ShowMessage(val message: String) : DetailEffect
}
