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
