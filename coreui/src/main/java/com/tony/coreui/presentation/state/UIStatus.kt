package com.tony.coreui.presentation.state

/**
 * High-level lifecycle states used by [UIState].
 *
 * These values describe the current rendering phase of a screen independently from the concrete
 * data type carried by the state object.
 */
enum class UIStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}
