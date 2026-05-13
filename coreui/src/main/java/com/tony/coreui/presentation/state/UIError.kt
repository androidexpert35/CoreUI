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
 */
data class UIError(
    val title: String,
    val message: String,
    val type: Any? = null,
    val retryAction: (() -> Unit)? = null
)
