package com.tony.coreui.presentation.components.basescreen

/**
 * Configuration for the default error dialog rendered by [AppBaseScreen].
 *
 * Use this type to customize action labels and callbacks while still relying on the built-in
 * dialog presentation. [onDismissRequest] is invoked after the higher-level dismiss callback
 * supplied to [AppBaseScreen] through `onErrorDialogDismiss`.
 *
 * @param onConfirm callback invoked when the confirm action is selected.
 * @param onCancel optional callback invoked when the dismiss action is selected.
 * @param onDismissRequest optional callback invoked after the dialog has been dismissed.
 * @param confirmButtonText optional label for the confirm action. When `null`, a default localized
 * string is used.
 * @param retryButtonText optional label for the retry action. When `null`, a default localized
 * string is used when a retry action is available.
 * @param dismissButtonText optional label for the dismiss action.
 */
data class ErrorDialogConfig(
    val onConfirm: () -> Unit = {},
    val onCancel: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,
    val confirmButtonText: String? = null,
    val retryButtonText: String? = null,
    val dismissButtonText: String? = null
)
