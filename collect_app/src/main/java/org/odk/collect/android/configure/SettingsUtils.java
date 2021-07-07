package org.odk.collect.android.configure;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.shared.Settings;

public class SettingsUtils {

    private SettingsUtils() {

    }

    @NonNull
    public static FormUpdateMode getFormUpdateMode(Context context, Settings generalSettings) {
        String protocol = generalSettings.getString(GeneralKeys.KEY_PROTOCOL);

        if (protocol.equals(GeneralKeys.PROTOCOL_GOOGLE_SHEETS)) {
            return FormUpdateMode.MANUAL;
        } else {
            String mode = generalSettings.getString(GeneralKeys.KEY_FORM_UPDATE_MODE);
            return FormUpdateMode.parse(context, mode);
        }
    }
}
