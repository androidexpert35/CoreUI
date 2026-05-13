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
 * Displays a reusable screen scaffold that coordinates content, loading, and error rendering.
 *
 * `AppBaseScreen` is designed as the default container for feature screens backed by a [UIState],
 * typically exposed from
 * [com.tony.coreui.presentation.viewmodel.BaseViewModel]. It applies system bar styling, renders
 * the latest successful content when available, and overlays loading or error UI according to the
 * current state.
 *
 * By default, loading feedback is rendered through [LoadingScreen] and failures are surfaced
 * through [BaseDialog]. Callers can replace either presentation with custom composable content
 * when a feature requires a different visual treatment.
 *
 * When the built-in error dialog is used, [onErrorDialogDismiss] is invoked whenever the dialog
 * is dismissed through one of its actions. This makes it easy to keep
 * [UIState.showErrorDialog] synchronized with the underlying state holder.
 *
 * @param uiState current screen state containing status, renderable data, and optional error
 * information.
 * @param statusBarColor color applied to the status bar while this screen is composed.
 * @param navigationBarColor color applied to the navigation bar while this screen is composed.
 * @param useLightStatusIcons when non-null, explicitly controls whether light status bar icons are
 * requested; otherwise the value is inferred from [statusBarColor].
 * @param useLightNavigationIcons when non-null, explicitly controls whether light navigation bar
 * icons are requested; otherwise the value is inferred from [navigationBarColor].
 * @param containerColor background color of the full-screen [Surface] that hosts this layout.
 * @param errorDialogConfig configuration used by the default error dialog when
 * [UIState.showErrorDialog] is `true`.
 * @param loadingType built-in loading presentation strategy to use when [UIStatus.LOADING] is
 * active.
 * @param loadingScreen optional custom loading content. When provided, it replaces the default
 * [LoadingScreen] for all enabled loading modes.
 * @param errorScreen optional custom full-screen error content shown when [uiState] is in the
 * error state and [UIError] is available.
 * @param onErrorDialogDismiss callback invoked when the built-in error dialog is dismissed.
 * @param content main render function that receives the latest non-null [UIState.data] value.
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
