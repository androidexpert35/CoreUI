package com.tony.coreui.components.basescreen

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
import com.tony.coreui.state.UIError
import com.tony.coreui.state.UIState
import com.tony.coreui.state.UIStatus

private const val EnterAnimationDurationMillis = 220
private const val ExitAnimationDurationMillis = 150

/**
 * Shared screen shell that handles loading and error overlays for Compose screens.
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
                    onDismissRequest = { errorDialogConfig.onDismissRequest?.invoke() },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                )
            }
        }
    }
}
