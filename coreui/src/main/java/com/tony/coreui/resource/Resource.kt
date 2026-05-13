package com.tony.coreui.resource

/**
 * Generic wrapper representing the outcome of a data operation.
 */
sealed class Resource<out T> {

    data class Success<T>(val data: T) : Resource<T>()

    data class Error(val data: ResourceError? = null) : Resource<Nothing>()
}

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> this
}

inline fun <T, R> Resource<T>.fold(
    onSuccess: (T) -> R,
    onError: (ResourceError?) -> R
): R = when (this) {
    is Resource.Success -> onSuccess(data)
    is Resource.Error -> onError(data)
}

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> = apply {
    if (this is Resource.Success) {
        action(data)
    }
}

inline fun <T> Resource<T>.onError(action: (ResourceError?) -> Unit): Resource<T> = apply {
    if (this is Resource.Error) {
        action(data)
    }
}

fun <T> Resource<T>.getOrNull(): T? = when (this) {
    is Resource.Success -> data
    is Resource.Error -> null
}
