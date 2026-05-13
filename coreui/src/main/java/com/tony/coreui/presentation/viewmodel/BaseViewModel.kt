package com.tony.coreui.presentation.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.coreui.R
import com.tony.coreui.data.strings.CoreUiStringProvider
import com.tony.coreui.domain.resource.Resource
import com.tony.coreui.domain.resource.ResourceError
import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.presentation.navigation.NavigationOptions
import com.tony.coreui.presentation.state.UIError
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
 */
abstract class BaseViewModel<UI_TYPE, UI_EVENT, UI_EFFECT>(
    private val navigationManager: NavigationManager? = null
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
     */
    protected open fun resolveString(@StringRes id: Int, vararg args: Any): String {
        return try {
            CoreUiStringProvider.get(id, *args)
        } catch (_: Throwable) {
            id.toString()
        }
    }

    /**
     * Maps an error payload to [UIError] and updates [uiState] accordingly.
     */
    protected open fun handleError(
        errorObject: Any,
        retryAction: (() -> Unit)? = null,
        processUiAfterError: ((UIError) -> UI_TYPE?)? = null,
        processUIError: ((UIError) -> UIError)? = null
    ) {
        val errorUiState = when (errorObject) {
            is Resource.Error -> processErrorResource(errorObject.data, retryAction)
            is Throwable -> UIError(
                title = resolveString(R.string.coreui_error_unexpected_title),
                message = errorObject.message
                    ?: resolveString(R.string.coreui_error_unknown_fallback_message),
                type = errorObject,
                retryAction = retryAction
            )
            else -> UIError(
                title = resolveString(R.string.coreui_error_unknown_title),
                message = resolveString(R.string.coreui_error_unknown_message),
                type = errorObject,
                retryAction = retryAction
            )
        }

        val processedError = processUIError?.invoke(errorUiState) ?: errorUiState

        _uiState.update { currentState ->
            val newData = processUiAfterError?.invoke(processedError)
            currentState.copy(
                status = UIStatus.ERROR,
                error = processedError,
                data = newData ?: currentState.data,
                showErrorDialog = processUiAfterError == null
            )
        }
    }

    /**
     * Converts a [ResourceError] into a localized [UIError].
     */
    protected open fun processErrorResource(
        resource: ResourceError?,
        retryAction: (() -> Unit)? = null
    ): UIError {
        return when (resource) {
            is ResourceError.LogicError -> UIError(
                title = resolveString(R.string.coreui_error_generic_title),
                message = resource.errorMessage
                    ?: resolveString(R.string.coreui_error_generic_fallback_message),
                type = resource,
                retryAction = retryAction
            )
            is ResourceError.ValidationError -> UIError(
                title = resolveString(R.string.coreui_error_validation_title),
                message = resource.message,
                type = resource,
                retryAction = retryAction
            )
            is ResourceError.StorageError -> UIError(
                title = resolveString(R.string.coreui_error_storage_title),
                message = resource.message,
                type = resource,
                retryAction = retryAction
            )
            is ResourceError.DatabaseError -> UIError(
                title = resolveString(R.string.coreui_error_database_title),
                message = resource.message,
                type = resource,
                retryAction = retryAction
            )
            is ResourceError.ServiceError -> UIError(
                title = resolveString(R.string.coreui_error_service_title),
                message = resource.message,
                type = resource,
                retryAction = retryAction
            )
            is ResourceError.NetworkError -> UIError(
                title = resolveString(R.string.coreui_error_network_title),
                message = resource.message.ifBlank {
                    resolveString(R.string.coreui_error_network_message)
                },
                type = resource,
                retryAction = retryAction
            )
            ResourceError.UnknownError, null -> UIError(
                title = resolveString(R.string.coreui_error_unknown_title),
                message = resolveString(R.string.coreui_error_unknown_message),
                type = resource,
                retryAction = retryAction
            )
        }
    }

    /**
     * Executes [dataFetchBlock], updates loading/success/error state and optionally invokes
     * [invokeOnCompletion] with the final success status.
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
     */
    fun onEvent(event: UI_EVENT) {
        handleEvent(event)
    }

    /**
     * Handles a UI event emitted by the screen.
     */
    protected abstract fun handleEvent(event: UI_EVENT)

    /**
     * Emits a one-off presentation effect.
     */
    protected fun emitEffect(effect: UI_EFFECT) {
        _uiEffect.tryEmit(effect)
    }

    /**
     * Shows or hides the default error dialog.
     *
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
     */
    protected fun updateUiData(newData: UI_TYPE?) {
        _uiState.update { it.copy(data = newData) }
    }

    /**
     * Applies a custom [UIState] transformation.
     */
    protected fun updateUiState(transform: (UIState<UI_TYPE>) -> UIState<UI_TYPE>) {
        _uiState.update(transform)
    }

    /**
     * Launches a coroutine tied to [viewModelScope] using the shared [exceptionHandler].
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
     */
    fun popBackStack(route: String? = null, inclusive: Boolean = false) {
        navigationManager?.popBackStack(route, inclusive)
    }

    /**
     * Delegates a navigation-and-clear-back-stack request to the configured [navigationManager],
     * if any.
     */
    fun navigateAndClearBackstackTo(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    ) {
        navigationManager?.navigateAndClearBackStack(route, popUpToRoute, inclusive)
    }
}
