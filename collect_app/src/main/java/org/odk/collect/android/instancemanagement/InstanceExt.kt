package org.odk.collect.android.instancemanagement

import org.odk.collect.forms.instances.Instance
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

const val OCTOBER_1st_2023_UTC = 1696118400000

fun Instance.canBeEdited(settingsProvider: SettingsProvider): Boolean {
    return (this.status == Instance.STATUS_INCOMPLETE || (this.status == Instance.STATUS_COMPLETE && this.lastStatusChangeDate < OCTOBER_1st_2023_UTC)) &&
        settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
}
