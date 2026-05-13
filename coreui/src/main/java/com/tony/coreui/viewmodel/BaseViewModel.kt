package com.tony.coreui.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.coreui.R
import com.tony.coreui.navigation.NavigationManager
import com.tony.coreui.navigation.NavigationOptions
import com.tony.coreui.resource.Resource
import com.tony.coreui.resource.ResourceError
import com.tony.coreui.state.UIError
import com.tony.coreui.state.UIState
import com.tony.coreui.state.UIStatus
import com.tony.coreui.strings.CoreUiStringProvider
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
 * Base view model with shared state, effect, error and navigation helpers.
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

    protected open fun resolveString(@StringRes id: Int, vararg args: Any): String {
        return try {
            CoreUiStringProvider.get(id, *args)
        } catch (_: Throwable) {
            id.toString()
        }
    }

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

    fun onEvent(event: UI_EVENT) {
        handleEvent(event)
    }

    protected abstract fun handleEvent(event: UI_EVENT)

    protected fun emitEffect(effect: UI_EFFECT) {
        _uiEffect.tryEmit(effect)
    }

    fun showErrorPopup(value: Boolean): UI_TYPE? {
        _uiState.update { it.copy(showErrorDialog = value) }
        return uiState.value.data
    }

    protected fun updateUiData(newData: UI_TYPE?) {
        _uiState.update { it.copy(data = newData) }
    }

    protected fun updateUiState(transform: (UIState<UI_TYPE>) -> UIState<UI_TYPE>) {
        _uiState.update(transform)
    }

    protected inline fun executeAsync(crossinline block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    protected fun setLoadingState() {
        _uiState.update {
            it.copy(
                status = UIStatus.LOADING,
                error = null,
                showErrorDialog = false
            )
        }
    }

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

    fun navigateToRoute(route: String, options: NavigationOptions = NavigationOptions()) {
        navigationManager?.navigate(route, options)
    }

    fun navigateUp() {
        navigationManager?.navigateUp()
    }

    fun popBackStack(route: String? = null, inclusive: Boolean = false) {
        navigationManager?.popBackStack(route, inclusive)
    }

    fun navigateAndClearBackstackTo(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = true
    ) {
        navigationManager?.navigateAndClearBackStack(route, popUpToRoute, inclusive)
    }
}
