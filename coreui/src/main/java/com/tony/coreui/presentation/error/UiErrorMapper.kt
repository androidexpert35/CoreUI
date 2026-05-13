package com.tony.coreui.presentation.error

import com.tony.coreui.R
import com.tony.coreui.data.strings.CoreUiStringProvider
import com.tony.coreui.data.strings.StringResolver
import com.tony.coreui.domain.resource.ResourceError
import com.tony.coreui.presentation.state.UIError

/**
 * Strategy interface that converts domain, infrastructure, or unexpected failures into
 * presentation-ready [UIError] values.
 */
interface UiErrorMapper {
    /**
     * Maps an arbitrary error payload into a [UIError].
     */
    fun map(errorObject: Any, retryAction: (() -> Unit)? = null): UIError

    /**
     * Maps a categorized [ResourceError] into a [UIError].
     */
    fun mapResourceError(resource: ResourceError?, retryAction: (() -> Unit)? = null): UIError
}

/**
 * Default [UiErrorMapper] used by the library when callers do not provide a custom strategy.
 *
 * The defaults preserve the current behavior while allowing hosts to replace the mapper entirely
 * when they need richer domain-specific messaging or alternate presentation policies.
 */
class DefaultUiErrorMapper(
    private val stringResolver: StringResolver = CoreUiStringProvider
) : UiErrorMapper {

    override fun map(errorObject: Any, retryAction: (() -> Unit)?): UIError {
        return when (errorObject) {
            is ResourceError -> mapResourceError(errorObject, retryAction)
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
    }

    override fun mapResourceError(resource: ResourceError?, retryAction: (() -> Unit)?): UIError {
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
            else -> UIError(
                title = resolveString(R.string.coreui_error_unknown_title),
                message = resolveString(R.string.coreui_error_unknown_message),
                type = resource,
                retryAction = retryAction,
                metadata = mapOf("resourceErrorType" to resource::class.qualifiedName)
            )
        }
    }

    private fun resolveString(id: Int, vararg args: Any): String {
        return try {
            stringResolver.get(id, *args)
        } catch (_: Throwable) {
            id.toString()
        }
    }
}
