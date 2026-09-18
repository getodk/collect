package org.odk.collect.shared.result

import org.odk.collect.shared.result.Result.Error
import org.odk.collect.shared.result.Result.Success
import kotlin.reflect.KClass

sealed class Result<out S, out E> {
    data class Success<S, E>(val value: S) : Result<S, E>()
    data class Error<S, E>(val value: E) : Result<S, E>()
}

@Throws(Exception::class)
fun <S, E> Result<S, E>.getOrThrow(block: (E) -> Throwable): S {
    return when (this) {
        is Success -> value
        is Error -> throw block(value)
    }
}

@Throws(Exception::class)
fun <S, E : Throwable> Result<S, E>.getOrThrow(): S {
    return when (this) {
        is Success -> value
        is Error -> throw value
    }
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

fun <S, E, T> Result<S, E>.mapError(map: (E) -> T): Result<S, T> {
    return when (this) {
        is Success -> this.value.toSuccess()
        is Error -> map(this.value).toError()
    }
}
