package org.odk.collect.android.backgroundwork.autosend

import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.forms.Form

class FormLevelAutoSendChecker(private val formsRepositoryProvider: FormsRepositoryProvider) {

    /**
     * Returns true if at least one form currently on the device specifies that all of its filled
     * forms should auto-send no matter the connection type.
     */
    fun isAutoSendEnabled(projectId: String): Boolean {
        return formsRepositoryProvider.get(projectId).all.any { form: Form ->
            form.autoSend.toBoolean()
        }
    }
}
