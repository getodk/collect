package org.odk.collect.android.preferences.utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.settings.keys.ProjectKeys;
import org.odk.collect.shared.settings.Settings;

public final class SettingsUtils {

    private SettingsUtils() {

    }

    @NonNull
    public static FormUpdateMode getFormUpdateMode(Context context, Settings generalSettings) {
        String protocol = generalSettings.getString(ProjectKeys.KEY_PROTOCOL);

        if (protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
            return FormUpdateMode.MANUAL;
        } else {
            String mode = generalSettings.getString(ProjectKeys.KEY_FORM_UPDATE_MODE);
            return FormUpdateMode.parse(context, mode);
        }
    }
}
