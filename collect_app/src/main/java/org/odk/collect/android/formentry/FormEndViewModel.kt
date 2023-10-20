package org.odk.collect.android.formentry

import androidx.lifecycle.ViewModel
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.android.instancemanagement.autosend.shouldFormBeSentAutomatically
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

class FormEndViewModel(
    private val formSessionRepository: FormSessionRepository,
    private val sessionId: String,
    private val settingsProvider: SettingsProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider
) : ViewModel() {

    fun isSaveDraftEnabled(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT)
    }

    fun isFinalizeEnabled(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY)
    }

    fun shouldFormBeSentAutomatically(): Boolean {
        val form = formSessionRepository.get(sessionId).value?.form
        return form?.shouldFormBeSentAutomatically(autoSendSettingsProvider.isAutoSendEnabledInSettings()) ?: false
    }
}
