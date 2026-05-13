package com.tony.coreui.domain.resource

/**
 * Represents the outcome of an operation exposed to the presentation layer.
 *
 * Use [Success] when a value is available and [Error] when the operation failed with a
 * categorized [ResourceError].
 */
sealed class Resource<out T> {

    /**
     * Successful operation containing [data].
     */
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * Failed operation containing an optional categorized [ResourceError].
     */
    data class Error(val data: ResourceError? = null) : Resource<Nothing>()
}

/**
 * Transforms the wrapped success value while preserving failure information.
 */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> this
}

/**
 * Folds this result into a single value.
 */
inline fun <T, R> Resource<T>.fold(
    onSuccess: (T) -> R,
    onError: (ResourceError?) -> R
): R = when (this) {
    is Resource.Success -> onSuccess(data)
    is Resource.Error -> onError(data)
}

/**
 * Executes [action] when this instance is [Resource.Success].
 */
inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> = apply {
    if (this is Resource.Success) {
        action(data)
    }
}

/**
 * Executes [action] when this instance is [Resource.Error].
 */
inline fun <T> Resource<T>.onError(action: (ResourceError?) -> Unit): Resource<T> = apply {
    if (this is Resource.Error) {
        action(data)
    }
}

/**
 * Returns the success value or `null` when this instance represents a failure.
 */
fun <T> Resource<T>.getOrNull(): T? = when (this) {
    is Resource.Success -> data
    is Resource.Error -> null
}
