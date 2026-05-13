package com.tony.coreui.presentation.state

/**
 * Preferred built-in error presentation for [UIError].
 *
 * Library defaults still favor dialogs, but consumers can opt into full-screen errors or suppress
 * built-in error UI entirely and render the state themselves.
 */
enum class UIErrorDisplayMode {
    DIALOG,
    FULL_SCREEN,
    NONE
}
