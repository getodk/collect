package org.odk.collect.android.javarosawrapper

import androidx.annotation.StringRes
import org.javarosa.core.model.FormIndex

sealed class ValidationResult

object SuccessValidationResult : ValidationResult()

data class FailedValidationResult(
    val index: FormIndex,
    val status: Int,
    val customErrorMessage: String?,
    @StringRes val defaultErrorMessage: Int
) : ValidationResult()
