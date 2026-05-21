package com.tony.coreui.sample.domain.model

data class DemoAlbumDetail(
    val id: Long,
    val title: String,
    val artist: String,
    val summary: String,
    val trackCount: Int,
    val lastUpdatedLabel: String,
    val defaultsHighlights: List<String>,
    val customizationHighlights: List<String>,
    val relatedAlbumId: Long?,
    val relatedAlbumTitle: String?
)
