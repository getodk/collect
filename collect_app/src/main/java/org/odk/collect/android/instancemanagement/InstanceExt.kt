package org.odk.collect.android.instancemanagement

import org.odk.collect.forms.instances.Instance
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

fun Instance.canBeEdited(settingsProvider: SettingsProvider): Boolean {
    return this.status == Instance.STATUS_INCOMPLETE &&
        settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
}

fun Instance.canBeEditedWithGracePeriod(settingsProvider: SettingsProvider): Boolean {
    return (this.status == Instance.STATUS_INCOMPLETE || this.status == Instance.STATUS_COMPLETE) &&
        settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
}
