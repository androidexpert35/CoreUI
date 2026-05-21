package com.tony.coreui.presentation.components.basescreen

import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.presentation.state.UIStatus

/**
 * Fully resolved UI layer state for a single [AppBaseScreen] render pass.
 *
 * @param T the screen data type.
 * @param contentState whether and what to show in the content layer.
 * @param showLoading whether the loading layer should be visible.
 * @param errorPresentation how the error layer should present the current error, if any.
 */
internal data class BaseScreenResolvedState<T>(
    val contentState: BaseScreenContentState<T>,
    val showLoading: Boolean,
    val errorPresentation: BaseScreenErrorPresentation = BaseScreenErrorPresentation.None
)

/** Discriminated state for the content layer of [AppBaseScreen]. */
internal sealed interface BaseScreenContentState<out T> {
    /**
     * Data is available and should be rendered.
     * @param value the current data to display.
     */
    data class Data<T>(val value: T) : BaseScreenContentState<T>

    /** No data is available; an empty-state composable should be shown if one is provided. */
    data object Empty : BaseScreenContentState<Nothing>

    /** The content layer should not be rendered at all. */
    data object Hidden : BaseScreenContentState<Nothing>
}

/** Discriminated state for the error layer of [AppBaseScreen]. */
internal sealed interface BaseScreenErrorPresentation {
    /**
     * A caller-supplied full-screen error composable should be shown.
     * @param error the error to display.
     */
    data class CustomScreen(val error: UIError) : BaseScreenErrorPresentation

    /**
     * The library's default [ErrorScreen] should be shown.
     * @param error the error to display.
     */
    data class BuiltInScreen(val error: UIError) : BaseScreenErrorPresentation

    /**
     * An error dialog (custom or built-in) should be shown.
     * @param error the error to display.
     */
    data class Dialog(val error: UIError) : BaseScreenErrorPresentation

    /** No error presentation is needed. */
    data object None : BaseScreenErrorPresentation
}

/**
 * Derives the three-layer render state for [AppBaseScreen] from the current [UIState].
 *
 * Error presentation is resolved in priority order:
 * 1. Caller-supplied [hasCustomErrorScreen] takes precedence over all built-in treatments.
 * 2. [UIErrorDisplayMode.FULL_SCREEN] triggers the built-in [ErrorScreen].
 * 3. [UIErrorDisplayMode.DIALOG] with [UIState.showErrorDialog] triggers the built-in dialog.
 * 4. Otherwise no error presentation is shown.
 *
 * Content visibility is then gated by whether the loading or error state should suppress it
 * according to [renderPolicy].
 *
 * @param T the screen data type.
 * @param uiState the current [UIState] to resolve.
 * @param renderPolicy controls visibility behaviour during loading and error states.
 * @param loadingType the active loading indicator type, used to decide content hiding.
 * @param hasEmptyContent whether the caller provided an empty-state composable.
 * @param hasCustomErrorScreen whether the caller provided a custom error-screen composable.
 * @return a [BaseScreenResolvedState] describing what each layer should render.
 */
internal fun <T> resolveBaseScreenState(
    uiState: UIState<T>,
    renderPolicy: BaseScreenRenderPolicy,
    loadingType: BaseLoadingType,
    hasEmptyContent: Boolean,
    hasCustomErrorScreen: Boolean
): BaseScreenResolvedState<T> {
    val status = uiState.status
    val data = uiState.data
    val error = uiState.error
    val isLoading = status == UIStatus.LOADING
    val isError = status == UIStatus.ERROR
    val showBuiltInErrorScreen =
        isError && error != null && error.displayMode == UIErrorDisplayMode.FULL_SCREEN
    val showBuiltInErrorDialog =
        isError &&
            error != null &&
            uiState.showErrorDialog &&
            error.displayMode == UIErrorDisplayMode.DIALOG

    val errorPresentation = when {
        hasCustomErrorScreen && isError && error != null ->
            BaseScreenErrorPresentation.CustomScreen(error)
        showBuiltInErrorScreen -> BaseScreenErrorPresentation.BuiltInScreen(error)
        showBuiltInErrorDialog -> BaseScreenErrorPresentation.Dialog(error)
        else -> BaseScreenErrorPresentation.None
    }

    val hideContentForDefaultLoading =
        isLoading &&
            loadingType == BaseLoadingType.DEFAULT &&
            renderPolicy.hideContentOnDefaultLoading
    val hideContentForBuiltInError =
        errorPresentation is BaseScreenErrorPresentation.BuiltInScreen &&
            data != null &&
            !renderPolicy.keepContentVisibleOnError

    val contentState = when {
        data != null && !hideContentForDefaultLoading && !hideContentForBuiltInError ->
            BaseScreenContentState.Data(data)
        data == null &&
            hasEmptyContent &&
            !isLoading &&
            (!isError || error?.displayMode == UIErrorDisplayMode.NONE) ->
            BaseScreenContentState.Empty
        else -> BaseScreenContentState.Hidden
    }

    return BaseScreenResolvedState(
        contentState = contentState,
        showLoading = isLoading && loadingType != BaseLoadingType.NONE,
        errorPresentation = errorPresentation
    )
}
