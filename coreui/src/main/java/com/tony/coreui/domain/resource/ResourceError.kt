package com.tony.coreui.domain.resource

/**
 * Canonical error categories recognized by the `coreui` module.
 *
 * Host applications with richer domain errors should map them to one of these categories before
 * surfacing the result through shared presentation components.
 */
sealed interface ResourceError {

    /**
     * Business-logic failure that does not fit a more specific category.
     */
    data class LogicError(
        val errorMessage: String?,
        val errorCode: String? = null
    ) : ResourceError

    /**
     * Validation failure associated with optional [field] metadata.
     */
    data class ValidationError(
        val message: String,
        val field: String? = null
    ) : ResourceError

    /**
     * Persistent storage failure related to local database access.
     */
    data class DatabaseError(
        val message: String
    ) : ResourceError

    /**
     * Persistent storage failure unrelated to database access.
     */
    data class StorageError(
        val message: String
    ) : ResourceError

    /**
     * Failure returned by a remote or platform service.
     */
    data class ServiceError(
        val message: String,
        val errorCode: String? = null
    ) : ResourceError

    /**
     * Network connectivity or transport failure.
     */
    data class NetworkError(
        val message: String,
        val httpCode: Int? = null
    ) : ResourceError

    /**
     * Fallback category when no meaningful classification is available.
     */
    data object UnknownError : ResourceError
}
