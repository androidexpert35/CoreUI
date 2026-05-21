package com.tony.coreui.sample.domain.model

data class DemoAlbum(
    val id: Long,
    val title: String,
    val artist: String,
    val summary: String,
    val trackCount: Int,
    val badge: String,
    val downloaded: Boolean,
    val focusReady: Boolean
)
