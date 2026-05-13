package com.tony.coreui.state

import androidx.compose.runtime.Stable

/**
 * Stable representation of a screen state combining lifecycle, content and error data.
 */
@Stable
data class UIState<T>(
    val status: UIStatus = UIStatus.IDLE,
    val data: T? = null,
    val error: UIError? = null,
    val showErrorDialog: Boolean = false
)
