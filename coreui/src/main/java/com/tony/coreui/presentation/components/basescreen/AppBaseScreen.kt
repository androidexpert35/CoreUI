package com.tony.coreui.presentation.components.basescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.tony.coreui.R
import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.presentation.state.UIStatus

private const val EnterAnimationDurationMillis = 220
private const val ExitAnimationDurationMillis = 150

/**
 * Reusable screen scaffold that orchestrates content, loading and error rendering.
 *
 * This composable is intended to be the default shell for feature screens that expose a [UIState]
 * from [com.tony.coreui.presentation.viewmodel.BaseViewModel].
 *
 * When the built-in error dialog is used, [onErrorDialogDismiss] is invoked whenever the dialog
 * is dismissed through one of its actions. This allows screens to keep the backing
 * [UIState.showErrorDialog] flag in sync without repeating the same close logic for every button.
 */
@Composable
fun <T> AppBaseScreen(
    uiState: UIState<T>,
    statusBarColor: Color = MaterialTheme.colorScheme.surface,
    navigationBarColor: Color = statusBarColor,
    useLightStatusIcons: Boolean? = null,
    useLightNavigationIcons: Boolean? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    errorDialogConfig: ErrorDialogConfig = ErrorDialogConfig(),
    loadingType: BaseLoadingType = BaseLoadingType.DEFAULT,
    loadingScreen: (@Composable () -> Unit)? = null,
    errorScreen: (@Composable (UIError) -> Unit)? = null,
    onErrorDialogDismiss: () -> Unit = {},
    content: @Composable (T) -> Unit
) {
    SystemAppearance(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        useLightStatusIcons = useLightStatusIcons,
        useLightNavigationIcons = useLightNavigationIcons
    )

    val status = uiState.status
    val data = uiState.data
    val error = uiState.error
    val isLoading = status == UIStatus.LOADING
    val isError = status == UIStatus.ERROR

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = containerColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (data != null) {
                val hideContentForDefaultLoading =
                    isLoading && loadingType == BaseLoadingType.DEFAULT
                if (!hideContentForDefaultLoading) {
                    content(data)
                }
            }

            AnimatedVisibility(
                visible = isLoading && loadingType != BaseLoadingType.NONE,
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

            if (errorScreen != null && isError && error != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = EnterAnimationDurationMillis)),
                    exit = fadeOut(animationSpec = tween(durationMillis = ExitAnimationDurationMillis))
                ) {
                    errorScreen(error)
                }
            } else if (isError && uiState.showErrorDialog && error != null) {
                val dismissErrorDialog = {
                    onErrorDialogDismiss()
                    errorDialogConfig.onDismissRequest?.invoke()
                    Unit
                }

                BaseDialog(
                    title = error.title,
                    message = error.message,
                    confirmButtonText = errorDialogConfig.confirmButtonText
                        ?: stringResource(R.string.coreui_action_ok),
                    retryButtonText = errorDialogConfig.retryButtonText
                        ?: stringResource(R.string.coreui_action_retry),
                    dismissButtonText = errorDialogConfig.dismissButtonText,
                    onConfirm = errorDialogConfig.onConfirm,
                    onRetry = error.retryAction,
                    onCancel = errorDialogConfig.onCancel,
                    onDismissRequest = dismissErrorDialog,
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                )
            }
        }
    }
}
