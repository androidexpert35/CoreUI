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
     *
     * @param errorMessage optional user-facing or diagnostic description of the failure.
     * @param errorCode optional machine-readable code associated with the failure.
     */
    data class LogicError(
        val errorMessage: String?,
        val errorCode: String? = null
    ) : ResourceError

    /**
     * Validation failure associated with optional [field] metadata.
     *
     * @param message description of the validation issue.
     * @param field optional field name associated with the validation failure.
     */
    data class ValidationError(
        val message: String,
        val field: String? = null
    ) : ResourceError

    /**
     * Persistent storage failure related to local database access.
     *
     * @param message description of the storage failure.
     */
    data class DatabaseError(
        val message: String
    ) : ResourceError

    /**
     * Persistent storage failure unrelated to database access.
     *
     * @param message description of the storage failure.
     */
    data class StorageError(
        val message: String
    ) : ResourceError

    /**
     * Failure returned by a remote or platform service.
     *
     * @param message description of the service failure.
     * @param errorCode optional machine-readable code returned by the service.
     */
    data class ServiceError(
        val message: String,
        val errorCode: String? = null
    ) : ResourceError

    /**
     * Network connectivity or transport failure.
     *
     * @param message description of the connectivity or transport issue.
     * @param httpCode optional HTTP status code associated with the failure.
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
