package com.tony.coreui.presentation.state

/**
 * Human-readable error model consumed by reusable presentation components.
 *
 * This type is intentionally presentation-focused and should contain already localized, user-ready
 * messaging suitable for dialogs and full-screen error surfaces.
 *
 * @param title short title suitable for dialogs and full-screen error surfaces.
 * @param message user-facing message describing the failure.
 * @param type optional original error payload for callers that need additional context.
 * @param retryAction optional action invoked when the user chooses to retry.
 * @param displayMode preferred built-in presentation for this error.
 * @param metadata optional structured context that hosts may use for custom rendering or logging.
 */
data class UIError(
    val title: String,
    val message: String,
    val type: Any? = null,
    val retryAction: (() -> Unit)? = null,
    val displayMode: UIErrorDisplayMode = UIErrorDisplayMode.DIALOG,
    val metadata: Map<String, Any?> = emptyMap()
)
