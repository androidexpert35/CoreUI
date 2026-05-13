package com.tony.coreui.resource

/**
 * Categorised error types for operations wrapped in [Resource.Error].
 *
 * TODO: If a host app needs more domain-specific categories, map them to these
 * core types before they reach the shared presentation layer.
 */
sealed interface ResourceError {

    data class LogicError(
        val errorMessage: String?,
        val errorCode: String? = null
    ) : ResourceError

    data class ValidationError(
        val message: String,
        val field: String? = null
    ) : ResourceError

    data class DatabaseError(
        val message: String
    ) : ResourceError

    data class StorageError(
        val message: String
    ) : ResourceError

    data class ServiceError(
        val message: String,
        val errorCode: String? = null
    ) : ResourceError

    data class NetworkError(
        val message: String,
        val httpCode: Int? = null
    ) : ResourceError

    data object UnknownError : ResourceError
}
