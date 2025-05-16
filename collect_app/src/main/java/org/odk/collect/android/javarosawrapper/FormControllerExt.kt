package org.odk.collect.android.javarosawrapper

import org.javarosa.form.api.FormEntryCaption
import org.javarosa.form.api.FormEntryPrompt

object FormControllerExt {

    @JvmStatic
    @Throws(RepeatsInFieldListException::class)
    fun FormController.getQuestionPrompts(): Array<FormEntryPrompt> {
        return getQuestionPrompts(getFormIndex())
    }

    @JvmStatic
    fun FormController.getGroupsForCurrentIndex(): Array<FormEntryCaption> {
        return getGroupsForIndex(getFormIndex())
    }
}
