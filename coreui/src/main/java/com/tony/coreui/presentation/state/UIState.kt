package com.tony.coreui.presentation.state

import androidx.compose.runtime.Stable

/**
 * Stable state holder consumed by `coreui` presentation components.
 *
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
