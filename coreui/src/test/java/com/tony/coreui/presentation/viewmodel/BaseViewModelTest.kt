package com.tony.coreui.presentation.viewmodel

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.tony.coreui.R
import com.tony.coreui.data.strings.StringResolver
import com.tony.coreui.domain.resource.Resource
import com.tony.coreui.domain.resource.ResourceError
import com.tony.coreui.presentation.error.DefaultUiErrorMapper
import com.tony.coreui.presentation.error.UiErrorMapper
import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.presentation.state.UIStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun resourceErrorsUseInjectedStringResolverByDefault() = runTest(dispatcher) {
        val viewModel = TestViewModel(
            stringResolver = FakeStringResolver(
                values = mapOf(
                    R.string.coreui_error_network_title to "Custom network title",
                    R.string.coreui_error_network_message to "Custom network message"
                )
            )
        )

        viewModel.triggerError(Resource.Error(ResourceError.NetworkError(message = "")))

        val state = viewModel.uiState.value
        assertEquals(UIStatus.ERROR, state.status)
        assertEquals("Custom network title", state.error?.title)
        assertEquals("Custom network message", state.error?.message)
        assertTrue(state.showErrorDialog)
    }

    @Test
    fun customMapperCanSuppressBuiltInDialogRendering() = runTest(dispatcher) {
        val viewModel = TestViewModel(
            uiErrorMapper = object : UiErrorMapper {
                override fun map(errorObject: Any, retryAction: (() -> Unit)?): UIError {
                    return UIError(
                        title = "Full screen",
                        message = "Render me elsewhere",
                        displayMode = UIErrorDisplayMode.FULL_SCREEN,
                        retryAction = retryAction
                    )
                }

                override fun mapResourceError(
                    resource: ResourceError?,
                    retryAction: (() -> Unit)?
                ): UIError = map(resource ?: ResourceError.UnknownError, retryAction)
            }
        )

        viewModel.triggerError(IllegalStateException("boom"))

        val state = viewModel.uiState.value
        assertEquals(UIStatus.ERROR, state.status)
        assertEquals(UIErrorDisplayMode.FULL_SCREEN, state.error?.displayMode)
        assertFalse(state.showErrorDialog)
    }

    private class TestViewModel(
        stringResolver: StringResolver = FakeStringResolver(),
        uiErrorMapper: UiErrorMapper = DefaultUiErrorMapper(stringResolver)
    ) : BaseViewModel<String, Unit, Unit>(
        stringResolver = stringResolver,
        uiErrorMapper = uiErrorMapper
    ) {
        override fun handleEvent(event: Unit) = Unit

        fun triggerError(errorObject: Any) {
            handleError(errorObject)
        }
    }

    private class FakeStringResolver(
        private val values: Map<Int, String> = emptyMap()
    ) : StringResolver {
        override fun get(@StringRes id: Int, vararg formatArgs: Any): String {
            return values[id] ?: "value-$id"
        }

        override fun getPlural(
            @PluralsRes id: Int,
            quantity: Int,
            vararg formatArgs: Any
        ): String = values[id] ?: "plural-$id-$quantity"
    }
}
