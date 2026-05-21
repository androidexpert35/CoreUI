package com.tony.coreui.presentation.state

/**
 * High-level lifecycle states used by [UIState].
 *
 * These values describe the current rendering phase of a screen independently from the concrete
 * data type carried by the state object.
 */
enum class UIStatus {
    /** No operation is in progress; the screen is waiting for user interaction or initial data. */
    IDLE,
    /** A data-fetching or long-running operation is in progress. */
    LOADING,
    /** The last operation completed successfully and [UIState.data] is available. */
    SUCCESS,
    /** The last operation failed; [UIState.error] describes the failure. */
    ERROR
}
