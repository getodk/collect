package org.odk.collect.android.javarosawrapper

import org.javarosa.core.model.FormIndex

sealed class ValidationResult

object SuccessValidationResult : ValidationResult()

data class FailedValidationResult(val index: FormIndex, val status: Int) : ValidationResult()
