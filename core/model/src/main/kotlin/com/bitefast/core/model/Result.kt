package com.bitefast.core.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Enterprise sealed interface representing asynchronous UI/domain operation state.
 * Supports exhaustive when expressions without requiring an else branch.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

/**
 * Transforms a cold Flow<T> into a Flow<Result<T>>,
 * emitting Loading upon start, Success for each value, and Error if an exception is caught.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it)) }

/**
 * Executes the given [action] if this [Result] is [Result.Success].
 */
inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Executes the given [action] if this [Result] is [Result.Error].
 */
inline fun <T> Result<T>.onError(action: (exception: Throwable) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}

/**
 * Executes the given [action] if this [Result] is [Result.Loading].
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) action()
    return this
}

/**
 * Returns the encapsulated value if this instance is [Result.Success], or null otherwise.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error, Result.Loading -> null
}

/**
 * Returns the encapsulated value if this instance is [Result.Success], or [defaultValue] otherwise.
 */
fun <T> Result<T>.getOrDefault(defaultValue: T): T = when (this) {
    is Result.Success -> data
    is Result.Error, Result.Loading -> defaultValue
}