package org.odk.collect.testshared

import org.odk.collect.shared.result.Result
import org.odk.collect.shared.result.Result.Success
import kotlin.reflect.KClass

@Throws(AssertionError::class)
fun <S, E : Any, C : E> Result<S, E>.requireError(clazz: KClass<C>): C {
    val error = when (this) {
        is Success -> throw AssertionError()
        is Result.Error -> value
    }

    return if (clazz.isInstance(error)) {
        error as C
    } else {
        throw AssertionError()
    }
}
