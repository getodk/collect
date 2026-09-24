package org.odk.collect.shared.result

import org.odk.collect.shared.result.Result.Error
import org.odk.collect.shared.result.Result.Success
import kotlin.reflect.KClass

/**
 * Alternative to Kotlin's [kotlin.Result] that provides a typed error/failure value. This is
 * essentially an implementation of an Either monad that uses "result" language replacing "left"
 * "right" with "error" and "success" respectively.
 *
 */
sealed class Result<out S, out E> {
    data class Success<S, E>(val value: S) : Result<S, E>()
    data class Error<S, E>(val value: E) : Result<S, E>()
}

fun <S, E> S.toSuccess(): Success<S, E> {
    return Success(this)
}

fun <S, E> E.toError(): Error<S, E> {
    return Error(this)
}

fun <T> runAndCatch(block: () -> T): Result<T, Exception> {
    return try {
        block().toSuccess()
    } catch (e: Exception) {
        e.toError()
    }
}

@Throws(AssertionError::class)
fun <S, E : Any, C : E> Result<S, E>.requireError(clazz: KClass<C>): C {
    val error = when (this) {
        is Success -> throw AssertionError()
        is Error -> value
    }

    return if (clazz.isInstance(error)) {
        error as C
    } else {
        throw AssertionError()
    }
}

inline fun <S, E> Result<S, E>.onSuccess(block: (S) -> Unit): Result<S, E> {
    when (this) {
        is Success -> block(value)
        is Error -> {}
    }

    return this
}

inline fun <S, E> Result<S, E>.onError(block: (E) -> Unit): Result<S, E> {
    when (this) {
        is Success -> {}
        is Error -> block(value)
    }

    return this
}

fun <S, E, T> Result<S, E>.map(map: (S) -> T): Result<T, E> {
    return when (this) {
        is Success -> map(value).toSuccess()
        is Error -> value.toError()
    }
}

fun <S, E, T> Result<S, E>.mapError(map: (E) -> T): Result<S, T> {
    return when (this) {
        is Success -> value.toSuccess()
        is Error -> map(value).toError()
    }
}

fun <S, E> Error<*, E>.asInstanceOf(): Result<S, E> {
    return this.value.toError()
}

fun <S1, E, S2> Result<S1, E>.chain(block: (S1) -> Result<S2, E>): Result<S2, E> {
    return when (this) {
        is Success -> block(value)
        is Error -> this.asInstanceOf()
    }
}

fun <S, E> result(block: () -> S): Result<S, E> {
    return block().toSuccess()
}
