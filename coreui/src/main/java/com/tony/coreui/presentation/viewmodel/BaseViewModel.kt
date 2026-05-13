package com.tony.coreui.presentation.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.coreui.data.strings.CoreUiStringProvider
import com.tony.coreui.data.strings.StringResolver
import com.tony.coreui.domain.resource.Resource
import com.tony.coreui.domain.resource.ResourceError
import com.tony.coreui.presentation.error.DefaultUiErrorMapper
import com.tony.coreui.presentation.error.UiErrorMapper
import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.presentation.navigation.NavigationOptions
import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.presentation.state.UIStatus
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base `ViewModel` for screens built on top of the `coreui` presentation primitives.
 *
 * It centralizes:
 * - screen state exposure through [uiState]
 * - one-off effects through [uiEffect]
 * - categorized error mapping from [Resource] to [UIError]
 * - navigation delegation through [NavigationManager]
 *
 * Subclasses are expected to implement [handleEvent] and use the provided helpers to update UI
 * state consistently across screens.
 *
 * @param UI_TYPE type of the renderable screen model exposed through [uiState].
 * @param UI_EVENT type of events accepted through [onEvent].
 * @param UI_EFFECT type of one-off effects emitted through [uiEffect].
 * @param navigationManager optional navigation delegate used by the built-in navigation helpers.
 */
abstract class BaseViewModel<UI_TYPE, UI_EVENT, UI_EFFECT>(
    private val navigationManager: NavigationManager? = null,
    private val stringResolver: StringResolver = CoreUiStringProvider,
    private val uiErrorMapper: UiErrorMapper = DefaultUiErrorMapper(stringResolver)
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState<UI_TYPE>())
    val uiState: StateFlow<UIState<UI_TYPE>> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UI_EFFECT>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<UI_EFFECT> = _uiEffect.asSharedFlow()

    protected open val logTag: String = this::class.java.simpleName
    protected open val logErrorMessage: String = "The ViewModel caught an error"

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            handleError(throwable)
            Log.e(logTag, "$logErrorMessage in exceptionHandler", throwable)
        }
    }

    /**
     * Converts a string resource id into a localized string.
     *
     * Subclasses may override this method to plug a different resolution strategy in tests or
     * host-specific integrations.
     *
     * @param id string resource identifier.
     * @param args optional formatting arguments supplied to the string resource.
     * @return localized string resolved from [id], or the numeric id when resolution fails.
     */
    protected open fun resolveString(@StringRes id: Int, vararg args: Any): String {
        return try {
            stringResolver.get(id, *args)
        } catch (_: Throwable) {
            id.toString()
        }
    }

    /**
     * Maps an error payload to [UIError] and updates [uiState] accordingly.
     *
     * @param errorObject error payload to convert into presentation state.
     * @param retryAction optional retry action attached to the resulting [UIError].
     * @param processUiAfterError optional transformer that can derive replacement UI data from the
     * resolved [UIError].
     * @param processUIError optional post-processing step used to customize the resolved [UIError].
     */
    protected open fun handleError(
        errorObject: Any,
        retryAction: (() -> Unit)? = null,
        processUiAfterError: ((UIError) -> UI_TYPE?)? = null,
        processUIError: ((UIError) -> UIError)? = null
    ) {
        val errorUiState = when (errorObject) {
            is Resource.Error -> processErrorResource(errorObject.data, retryAction)
            else -> mapErrorObject(errorObject, retryAction)
        }

        val processedError = processUIError?.invoke(errorUiState) ?: errorUiState

        setErrorState(processedError, processUiAfterError)
    }

    /**
     * Maps an arbitrary error payload to a [UIError].
     *
     * Subclasses can override this method to specialize only a subset of failure types while
     * still delegating the rest to the injected [uiErrorMapper].
     */
    protected open fun mapErrorObject(
        errorObject: Any,
        retryAction: (() -> Unit)? = null
    ): UIError = uiErrorMapper.map(errorObject, retryAction)

    /**
     * Converts a [ResourceError] into a localized [UIError].
     *
     * @param resource categorized domain or data-layer error.
     * @param retryAction optional retry action attached to the resulting [UIError].
     * @return localized presentation error derived from [resource].
     */
    protected open fun processErrorResource(
        resource: ResourceError?,
        retryAction: (() -> Unit)? = null
    ): UIError = uiErrorMapper.mapResourceError(resource, retryAction)

    /**
     * Commits the provided [error] to [uiState] using the library defaults for error visibility.
     *
     * Hosts that want to set a presentation-ready error without going through [handleError] can
     * use this helper directly.
     */
    protected fun setErrorState(
        error: UIError,
        processUiAfterError: ((UIError) -> UI_TYPE?)? = null
    ) {
        _uiState.update { currentState ->
            val newData = processUiAfterError?.invoke(error)
            currentState.copy(
                status = UIStatus.ERROR,
                error = error,
                data = newData ?: currentState.data,
                showErrorDialog =
                    processUiAfterError == null && error.displayMode == UIErrorDisplayMode.DIALOG
            )
        }
    }

    /**
     * Executes [dataFetchBlock], updates loading/success/error state and optionally invokes
     * [invokeOnCompletion] with the final success status.
     *
     * @param retryAction optional retry action attached to the resulting error state.
     * @param dataFetchBlock suspend block that returns a [Resource] for the current request.
     * @param processSuccess mapper that converts a successful resource payload into [UI_TYPE].
     * @param updateUiAfterError optional transformer that can derive replacement UI data from the
     * resulting [UIError].
     * @param invokeOnCompletion optional callback invoked with `true` on success and `false` when
     * the operation fails.
     * @param skipLoading when `true`, prevents the automatic transition to [UIStatus.LOADING].
     */
    protected fun <RESOURCE> launchUiStateUpdate(
        retryAction: (() -> Unit)? = null,
        dataFetchBlock: suspend () -> Resource<RESOURCE>,
        processSuccess: (RESOURCE) -> UI_TYPE,
        updateUiAfterError: ((UIError) -> UI_TYPE?)? = null,
        invokeOnCompletion: ((success: Boolean) -> Unit)? = null,
        skipLoading: Boolean = false
    ) {
        viewModelScope.launch(exceptionHandler) {
            if (!skipLoading) {
                setLoadingState()
            }
            when (val resource = dataFetchBlock()) {
                is Resource.Success -> {
                    val newData = processSuccess(resource.data)
                    _uiState.update {
                        it.copy(
                            status = UIStatus.SUCCESS,
                            data = newData,
                            error = null,
                            showErrorDialog = false
                        )
                    }
                    invokeOnCompletion?.invoke(true)
                }
                is Resource.Error -> handleError(
                    errorObject = resource,
                    retryAction = retryAction,
                    processUiAfterError = updateUiAfterError
                )
            }
        }.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Log.e(logTag, "Coroutine completed with error", throwable)
                invokeOnCompletion?.invoke(false)
            }
        }
    }

    /**
     * Entry point for external UI events.
     *
     * @param event event emitted by the UI layer.
     */
    fun onEvent(event: UI_EVENT) {
        handleEvent(event)
    }

    /**
     * Handles a UI event emitted by the screen.
     *
     * @param event event emitted by the UI layer.
     */
    protected abstract fun handleEvent(event: UI_EVENT)

    /**
     * Emits a one-off presentation effect.
     *
     * @param effect effect instance to emit.
     */
    protected fun emitEffect(effect: UI_EFFECT) {
        _uiEffect.tryEmit(effect)
    }

    /**
     * Shows or hides the default error dialog.
     *
     * @param value `true` to show the dialog, `false` to hide it.
     * @return the latest UI data after the flag update.
     */
    fun showErrorPopup(value: Boolean): UI_TYPE? {
        _uiState.update { it.copy(showErrorDialog = value) }
        return uiState.value.data
    }

    /**
     * Hides the default error dialog exposed by [uiState].
     *
     * This convenience method is designed to be passed directly to presentation callbacks such as
     * `AppBaseScreen(onErrorDialogDismiss = viewModel::dismissErrorPopup)`.
     */
    fun dismissErrorPopup() {
        showErrorPopup(false)
    }

    /**
     * Replaces the current [UIState.data] value.
     *
     * @param newData new renderable data to expose.
     */
    protected fun updateUiData(newData: UI_TYPE?) {
        _uiState.update { it.copy(data = newData) }
    }

    /**
     * Applies a custom [UIState] transformation.
     *
     * @param transform state transformation applied atomically to the current value.
     */
    protected fun updateUiState(transform: (UIState<UI_TYPE>) -> UIState<UI_TYPE>) {
        _uiState.update(transform)
    }

    /**
     * Launches a coroutine tied to [viewModelScope] using the shared [exceptionHandler].
     *
     * @param block suspend work to execute.
     */
    protected inline fun executeAsync(crossinline block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    /**
     * Marks the screen as loading and clears any current error.
     */
    protected fun setLoadingState() {
        _uiState.update {
            it.copy(
                status = UIStatus.LOADING,
                error = null,
                showErrorDialog = false
            )
        }
    }

    /**
     * Marks the screen as successful and optionally replaces its renderable data.
     *
     * @param newData optional renderable data to expose with the success state.
     */
    protected fun setSuccessState(newData: UI_TYPE? = uiState.value.data) {
        _uiState.update {
            it.copy(
                status = UIStatus.SUCCESS,
                data = newData,
                error = null,
                showErrorDialog = false
            )
        }
    }

    /**
     * Marks the screen as idle and optionally replaces its renderable data.
     *
     * @param newData optional renderable data to expose with the idle state.
     */
    protected fun setIdleState(newData: UI_TYPE? = uiState.value.data) {
        _uiState.update {
            it.copy(
                status = UIStatus.IDLE,
                data = newData,
                error = null,
                showErrorDialog = false
            )
        }
    }

    /**
     * Delegates a navigation request to the configured [navigationManager], if any.
     *
     * @param route logical destination identifier understood by the host navigator.
     * @param options framework-agnostic navigation options associated with the request.
     */
    fun navigateToRoute(route: String, options: NavigationOptions = NavigationOptions()) {
        navigationManager?.navigate(route, options)
    }

    /**
     * Delegates an upward navigation request to the configured [navigationManager], if any.
     */
    fun navigateUp() {
        navigationManager?.navigateUp()
    }

    /**
     * Delegates a back stack pop request to the configured [navigationManager], if any.
     *
     * @param route optional route that acts as the pop target.
     * @param inclusive whether [route], when provided, should also be removed.
     */
    fun popBackStack(route: String? = null, inclusive: Boolean = false) {
        navigationManager?.popBackStack(route, inclusive)
    }

    /**
     * Delegates a navigation-and-clear-back-stack request to the configured [navigationManager],
     * if any.
     *
     * @param route logical destination identifier understood by the host navigator.
     * @param popUpToRoute optional route used as the boundary for the clear-back-stack operation.
     * @param inclusive whether [popUpToRoute], when provided, should also be removed.
     */
    fun navigateAndClearBackstackTo(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    ) {
        navigationManager?.navigateAndClearBackStack(route, popUpToRoute, inclusive)
    }
}
