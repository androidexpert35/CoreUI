package com.tony.coreui.presentation.state

/**
 * Preferred built-in error presentation for [UIError].
 *
 * Library defaults still favor dialogs, but consumers can opt into full-screen errors or suppress
 * built-in error UI entirely and render the state themselves.
 */
enum class UIErrorDisplayMode {
    /** Show the error in a dialog overlay, leaving any underlying content visible. */
    DIALOG,
    /** Replace the current screen content with a dedicated full-screen error layout. */
    FULL_SCREEN,
    /** Suppress all built-in error UI; the consumer is responsible for reacting to the error. */
    NONE
}
