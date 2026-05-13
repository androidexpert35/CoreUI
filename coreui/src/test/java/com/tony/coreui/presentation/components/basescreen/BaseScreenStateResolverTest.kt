package com.tony.coreui.presentation.components.basescreen

import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.presentation.state.UIStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseScreenStateResolverTest {

    @Test
    fun defaultLoadingHidesContentWhileShowingLoadingLayer() {
        val resolvedState = resolveBaseScreenState(
            uiState = UIState(status = UIStatus.LOADING, data = "ready"),
            renderPolicy = BaseScreenRenderPolicy(),
            loadingType = BaseLoadingType.DEFAULT,
            hasEmptyContent = false,
            hasCustomErrorScreen = false
        )

        assertTrue(resolvedState.contentState is BaseScreenContentState.Hidden)
        assertTrue(resolvedState.showLoading)
        assertEquals(BaseScreenErrorPresentation.None, resolvedState.errorPresentation)
    }

    @Test
    fun overlayLoadingKeepsLatestContentVisible() {
        val resolvedState = resolveBaseScreenState(
            uiState = UIState(status = UIStatus.LOADING, data = "ready"),
            renderPolicy = BaseScreenRenderPolicy(),
            loadingType = BaseLoadingType.OVERLAY,
            hasEmptyContent = false,
            hasCustomErrorScreen = false
        )

        assertEquals("ready", (resolvedState.contentState as BaseScreenContentState.Data).value)
        assertTrue(resolvedState.showLoading)
    }

    @Test
    fun builtInFullScreenErrorHidesContentWhenPolicyDisablesVisibility() {
        val resolvedState = resolveBaseScreenState(
            uiState = UIState(
                status = UIStatus.ERROR,
                data = "ready",
                error = UIError(
                    title = "Oops",
                    message = "Something failed",
                    displayMode = UIErrorDisplayMode.FULL_SCREEN
                )
            ),
            renderPolicy = BaseScreenRenderPolicy(keepContentVisibleOnError = false),
            loadingType = BaseLoadingType.NONE,
            hasEmptyContent = false,
            hasCustomErrorScreen = false
        )

        assertTrue(resolvedState.contentState is BaseScreenContentState.Hidden)
        assertEquals(
            BaseScreenErrorPresentation.BuiltInScreen(
                UIError(
                    title = "Oops",
                    message = "Something failed",
                    displayMode = UIErrorDisplayMode.FULL_SCREEN
                )
            ),
            resolvedState.errorPresentation
        )
    }

    @Test
    fun builtInFullScreenErrorKeepsContentVisibleByDefault() {
        val resolvedState = resolveBaseScreenState(
            uiState = UIState(
                status = UIStatus.ERROR,
                data = "ready",
                error = UIError(
                    title = "Oops",
                    message = "Something failed",
                    displayMode = UIErrorDisplayMode.FULL_SCREEN
                )
            ),
            renderPolicy = BaseScreenRenderPolicy(),
            loadingType = BaseLoadingType.NONE,
            hasEmptyContent = false,
            hasCustomErrorScreen = false
        )

        assertEquals("ready", (resolvedState.contentState as BaseScreenContentState.Data).value)
        assertTrue(resolvedState.errorPresentation is BaseScreenErrorPresentation.BuiltInScreen)
    }

    @Test
    fun emptySlotCanRenderWhenErrorUiIsSuppressed() {
        val resolvedState = resolveBaseScreenState(
            uiState = UIState(
                status = UIStatus.ERROR,
                error = UIError(
                    title = "Silent",
                    message = "Handled elsewhere",
                    displayMode = UIErrorDisplayMode.NONE
                )
            ),
            renderPolicy = BaseScreenRenderPolicy(),
            loadingType = BaseLoadingType.NONE,
            hasEmptyContent = true,
            hasCustomErrorScreen = false
        )

        assertEquals(BaseScreenContentState.Empty, resolvedState.contentState)
        assertEquals(BaseScreenErrorPresentation.None, resolvedState.errorPresentation)
    }
}
