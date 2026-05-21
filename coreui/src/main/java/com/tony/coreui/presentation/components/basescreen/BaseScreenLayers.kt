package com.tony.coreui.presentation.components.basescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.tony.coreui.R
import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIState

private const val EnterAnimationDurationMillis = 220
private const val ExitAnimationDurationMillis = 150

/**
 * Renders the content layer of [AppBaseScreen].
 *
 * Delegates to [contentWithState] when both data and [UIState] context are needed, to [content]
 * for data-only rendering, or to [emptyContent] when no data is available and an empty-state
 * composable has been provided. Renders nothing when the state is [BaseScreenContentState.Hidden].
 *
 * @param T the screen data type.
 * @param resolvedState the pre-computed layer state produced by [resolveBaseScreenState].
 * @param uiState the current full [UIState], forwarded to [contentWithState].
 * @param emptyContent optional composable shown while [BaseScreenContentState.Empty].
 * @param contentWithState optional composable for rendering data alongside the full [UIState].
 * @param content primary composable for rendering data without [UIState] access.
 */
@Composable
internal fun <T> BaseScreenContentLayer(
    resolvedState: BaseScreenResolvedState<T>,
    uiState: UIState<T>,
    emptyContent: (@Composable () -> Unit)?,
    contentWithState: (@Composable (T, UIState<T>) -> Unit)?,
    content: @Composable (T) -> Unit
) {
    when (val contentState = resolvedState.contentState) {
        is BaseScreenContentState.Data ->
            contentWithState?.invoke(contentState.value, uiState) ?: content(contentState.value)
        BaseScreenContentState.Empty -> emptyContent?.invoke()
        BaseScreenContentState.Hidden -> Unit
    }
}

/**
 * Renders the loading layer of [AppBaseScreen] with an enter/exit fade animation.
 *
 * A custom [loadingScreen], when provided, takes full precedence over the built-in options.
 * For [BaseLoadingType.OVERLAY] the built-in [LoadingScreen] is rendered with a scrim tint.
 * [BaseLoadingType.NONE] suppresses all output even when [showLoading] is `true`.
 *
 * @param showLoading whether the loading layer is currently active.
 * @param loadingType the built-in loading style to use when no custom screen is provided.
 * @param loadingScreen optional composable that replaces the built-in loading UI entirely.
 */
@Composable
internal fun BaseScreenLoadingLayer(
    showLoading: Boolean,
    loadingType: BaseLoadingType,
    loadingScreen: (@Composable () -> Unit)?
) {
    AnimatedVisibility(
        visible = showLoading,
        enter = fadeIn(animationSpec = tween(durationMillis = EnterAnimationDurationMillis)),
        exit = fadeOut(animationSpec = tween(durationMillis = ExitAnimationDurationMillis))
    ) {
        if (loadingScreen != null) {
            loadingScreen()
        } else {
            when (loadingType) {
                BaseLoadingType.DEFAULT -> LoadingScreen()
                BaseLoadingType.OVERLAY -> LoadingScreen(
                    backgroundColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
                )
                BaseLoadingType.NONE -> Unit
            }
        }
    }
}

/**
 * Renders the error presentation layer of [AppBaseScreen].
 *
 * The exact presentation depends on [errorPresentation]:
 * - [BaseScreenErrorPresentation.CustomScreen] — calls the caller-supplied [errorScreen].
 * - [BaseScreenErrorPresentation.BuiltInScreen] — renders the library's default [ErrorScreen].
 * - [BaseScreenErrorPresentation.Dialog] — shows [errorDialog] if provided, otherwise falls back
 *   to the built-in [BaseDialog].
 * - [BaseScreenErrorPresentation.None] — renders nothing.
 *
 * @param errorPresentation the resolved error layer state.
 * @param errorDialogConfig configuration applied to the built-in dialog, including button labels
 * and callbacks.
 * @param dialogProperties [DialogProperties] forwarded to the built-in dialog.
 * @param errorDialog optional composable for a fully custom error dialog.
 * @param errorScreen optional composable for a fully custom full-screen error layout.
 * @param onErrorDialogDismiss callback invoked when the dialog is dismissed via any action.
 */
@Composable
internal fun BaseScreenErrorLayer(
    errorPresentation: BaseScreenErrorPresentation,
    errorDialogConfig: ErrorDialogConfig,
    dialogProperties: DialogProperties,
    errorDialog: (@Composable (UIError, () -> Unit) -> Unit)?,
    errorScreen: (@Composable (UIError) -> Unit)?,
    onErrorDialogDismiss: () -> Unit
) {
    when (errorPresentation) {
        is BaseScreenErrorPresentation.CustomScreen -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = EnterAnimationDurationMillis)),
                exit = fadeOut(animationSpec = tween(durationMillis = ExitAnimationDurationMillis))
            ) {
                errorScreen?.invoke(errorPresentation.error)
            }
        }

        is BaseScreenErrorPresentation.BuiltInScreen -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = EnterAnimationDurationMillis)),
                exit = fadeOut(animationSpec = tween(durationMillis = ExitAnimationDurationMillis))
            ) {
                ErrorScreen(
                    title = errorPresentation.error.title,
                    description = errorPresentation.error.message,
                    primaryButtonText = if (errorPresentation.error.retryAction != null) {
                        stringResource(R.string.coreui_action_retry)
                    } else {
                        null
                    },
                    onPrimaryButtonClick = errorPresentation.error.retryAction
                )
            }
        }

        is BaseScreenErrorPresentation.Dialog -> {
            val dismissErrorDialog = {
                onErrorDialogDismiss()
                errorDialogConfig.onDismissRequest?.invoke()
                Unit
            }

            if (errorDialog != null) {
                errorDialog(errorPresentation.error, dismissErrorDialog)
            } else {
                BaseDialog(
                    title = errorPresentation.error.title,
                    message = errorPresentation.error.message,
                    confirmButtonText = errorDialogConfig.confirmButtonText
                        ?: stringResource(R.string.coreui_action_ok),
                    retryButtonText = errorDialogConfig.retryButtonText
                        ?: stringResource(R.string.coreui_action_retry),
                    dismissButtonText = errorDialogConfig.dismissButtonText,
                    onConfirm = errorDialogConfig.onConfirm,
                    onRetry = errorDialogConfig.onRetry ?: errorPresentation.error.retryAction,
                    onCancel = errorDialogConfig.onCancel,
                    onDismissRequest = dismissErrorDialog,
                    properties = dialogProperties
                )
            }
        }

        BaseScreenErrorPresentation.None -> Unit
    }
}
