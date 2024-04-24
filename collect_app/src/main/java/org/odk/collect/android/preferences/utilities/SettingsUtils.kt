package org.odk.collect.android.preferences.utilities

import android.content.Context
import org.odk.collect.settings.enums.AutoSend
import org.odk.collect.settings.enums.AutoSend.Companion.parse
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

object SettingsUtils {

    @JvmStatic
    fun getFormUpdateMode(context: Context, settings: Settings): FormUpdateMode {
        val setting = settings.getString(ProjectKeys.KEY_FORM_UPDATE_MODE)
        return FormUpdateMode.parse(context, setting)
    }

    @JvmStatic
    fun Settings.getAutoSend(context: Context): AutoSend {
        val setting = this.getString(ProjectKeys.KEY_AUTOSEND)
        return parse(context, setting)
    }
}
