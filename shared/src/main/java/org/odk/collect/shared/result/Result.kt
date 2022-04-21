package org.odk.collect.shared.result

sealed class Result<S, E> {
    data class Success<S, E>(val value: S) : Result<S, E>()
    data class Error<S, E>(val value: E) : Result<S, E>()

    companion object {
        fun <S, E> S.toSuccess(): Success<S, E> {
            return Success(this)
        }

        fun <S, E> E.toError(): Error<S, E> {
            return Error(this)
        }
    }
}
