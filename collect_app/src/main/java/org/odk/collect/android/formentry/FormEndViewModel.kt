package org.odk.collect.android.formentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.android.instancemanagement.autosend.shouldFormBeSentAutomatically
import org.odk.collect.forms.Form
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

class FormEndViewModel(
    private val settingsProvider: SettingsProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider
) : ViewModel() {

    fun isSaveDraftEnabled(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT)
    }

    fun isFinalizeEnabled(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_FINALIZE)
    }

    fun shouldFormBeSentAutomatically(form: Form): Boolean {
        return form.shouldFormBeSentAutomatically(autoSendSettingsProvider.isAutoSendEnabledInSettings())
    }

    class Factory(private val settingsProvider: SettingsProvider, private val autoSendSettingsProvider: AutoSendSettingsProvider) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormEndViewModel(settingsProvider, autoSendSettingsProvider) as T
        }
    }
}
