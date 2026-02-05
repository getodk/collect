package org.odk.collect.android.formentry

import org.javarosa.core.model.FormIndex
import org.odk.collect.android.javarosawrapper.ValidationResult

data class CurrentFormIndex(
    /**
     * The index of the screen to navigate to or refresh.
     */
    val screenIndex: FormIndex,
    /**
     * The index of the particular question to focus on and scroll to within a screen
     * containing multiple questions. Can be null if navigation is intended
     * only to the screen without focusing on a specific question.
     */
    val questionIndex: FormIndex?,
    /**
     * The result of validating the current form state.
     * Contains information necessary to highlight a question with an invalid
     * answer if validation was performed and failed. Can be null if validation
     * has not been executed.
     */
    val validationResult: ValidationResult?
)
