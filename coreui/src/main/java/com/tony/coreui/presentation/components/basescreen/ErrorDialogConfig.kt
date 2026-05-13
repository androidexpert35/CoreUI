package com.tony.coreui.presentation.components.basescreen

/**
 * Configuration for the default dialog rendered by [AppBaseScreen] when an error is present.
 *
 * [onDismissRequest] is invoked after the dialog-level dismiss callback supplied to
 * [AppBaseScreen] through `onErrorDialogDismiss`.
 */
data class ErrorDialogConfig(
    val onConfirm: () -> Unit = {},
    val onCancel: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,
    val confirmButtonText: String? = null,
    val retryButtonText: String? = null,
    val dismissButtonText: String? = null
)
