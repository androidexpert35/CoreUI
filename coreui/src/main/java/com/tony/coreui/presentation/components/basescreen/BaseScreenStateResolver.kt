package com.tony.coreui.presentation.components.basescreen

import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.presentation.state.UIStatus

internal data class BaseScreenResolvedState<T>(
    val contentState: BaseScreenContentState<T>,
    val showLoading: Boolean,
    val errorPresentation: BaseScreenErrorPresentation = BaseScreenErrorPresentation.None
)

internal sealed interface BaseScreenContentState<out T> {
    data class Data<T>(val value: T) : BaseScreenContentState<T>

    data object Empty : BaseScreenContentState<Nothing>

    data object Hidden : BaseScreenContentState<Nothing>
}

internal sealed interface BaseScreenErrorPresentation {
    data class CustomScreen(val error: UIError) : BaseScreenErrorPresentation

    data class BuiltInScreen(val error: UIError) : BaseScreenErrorPresentation

    data class Dialog(val error: UIError) : BaseScreenErrorPresentation

    data object None : BaseScreenErrorPresentation
}

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
