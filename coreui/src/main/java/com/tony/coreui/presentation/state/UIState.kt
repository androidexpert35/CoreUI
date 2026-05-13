package com.tony.coreui.presentation.state

import androidx.compose.runtime.Stable

/**
 * Stable state holder consumed by `coreui` presentation components.
 *
 * This type is intended to model the full render state of a screen, combining lifecycle status,
 * the latest successful data payload, and optional user-facing error information.
 *
 * @param T type of the renderable screen model exposed to the UI.
 * @param status current lifecycle phase of the screen.
 * @param data current renderable data, if available.
 * @param error human-readable error information associated with the latest failure.
 * @param showErrorDialog whether the default error dialog should be displayed.
 */
@Stable
data class UIState<T>(
    val status: UIStatus = UIStatus.IDLE,
    val data: T? = null,
    val error: UIError? = null,
    val showErrorDialog: Boolean = false
)
