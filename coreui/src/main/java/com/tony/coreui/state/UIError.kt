package com.tony.coreui.state

/**
 * Human-readable error information surfaced to the UI layer.
 */
data class UIError(
    val title: String,
    val message: String,
    val type: Any? = null,
    val retryAction: (() -> Unit)? = null
)
