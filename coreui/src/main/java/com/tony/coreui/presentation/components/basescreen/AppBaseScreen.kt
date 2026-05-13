package com.tony.coreui.presentation.components.basescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.presentation.state.UIStatus

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
 * @param renderPolicy rendering policy that controls how strongly the scaffold owns system
 * appearance and content visibility.
 * @param errorDialogConfig configuration used by the default error dialog when
 * [UIState.showErrorDialog] is `true`.
 * @param loadingType built-in loading presentation strategy to use when [UIStatus.LOADING] is
 * active.
 * @param loadingScreen optional custom loading content. When provided, it replaces the default
 * [LoadingScreen] for all enabled loading modes.
 * @param emptyContent optional content shown when no renderable data is available and the state is
 * neither loading nor showing a built-in error presentation.
 * @param errorDialog optional custom dialog content used when [UIState.showErrorDialog] is `true`.
 * @param errorScreen optional custom full-screen error content shown when [uiState] is in the
 * error state and [UIError] is available.
 * @param contentWithState optional render function that receives both the latest non-null
 * [UIState.data] value and the full [uiState].
 * @param onErrorDialogDismiss callback invoked when the built-in error dialog is dismissed.
 * @param dialogProperties [DialogProperties] applied to the built-in error dialog.
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
    renderPolicy: BaseScreenRenderPolicy = BaseScreenRenderPolicy(),
    errorDialogConfig: ErrorDialogConfig = ErrorDialogConfig(),
    loadingType: BaseLoadingType = BaseLoadingType.DEFAULT,
    loadingScreen: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    errorDialog: (@Composable (UIError, () -> Unit) -> Unit)? = null,
    errorScreen: (@Composable (UIError) -> Unit)? = null,
    contentWithState: (@Composable (T, UIState<T>) -> Unit)? = null,
    onErrorDialogDismiss: () -> Unit = {},
    dialogProperties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    ),
    content: @Composable (T) -> Unit
) {
    if (renderPolicy.applySystemAppearance) {
        SystemAppearance(
            statusBarColor = statusBarColor,
            navigationBarColor = navigationBarColor,
            useLightStatusIcons = useLightStatusIcons,
            useLightNavigationIcons = useLightNavigationIcons
        )
    }

    val resolvedState = resolveBaseScreenState(
        uiState = uiState,
        renderPolicy = renderPolicy,
        loadingType = loadingType,
        hasEmptyContent = emptyContent != null,
        hasCustomErrorScreen = errorScreen != null
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = containerColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BaseScreenContentLayer(
                resolvedState = resolvedState,
                uiState = uiState,
                emptyContent = emptyContent,
                contentWithState = contentWithState,
                content = content
            )

            BaseScreenLoadingLayer(
                showLoading = resolvedState.showLoading,
                loadingType = loadingType,
                loadingScreen = loadingScreen
            )

            BaseScreenErrorLayer(
                errorPresentation = resolvedState.errorPresentation,
                errorDialogConfig = errorDialogConfig,
                dialogProperties = dialogProperties,
                errorDialog = errorDialog,
                errorScreen = errorScreen,
                onErrorDialogDismiss = onErrorDialogDismiss
            )
        }
    }
}
