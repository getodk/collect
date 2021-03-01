package org.odk.collect.android.configure;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.android.preferences.Protocol;

public class SettingsUtils {

    private SettingsUtils() {

    }

    @NonNull
    public static FormUpdateMode getFormUpdateMode(Context context, PreferencesDataSource generalPreferences) {
        String protocol = generalPreferences.getString(GeneralKeys.KEY_PROTOCOL);

        if (Protocol.parse(context, protocol) == Protocol.GOOGLE) {
            return FormUpdateMode.MANUAL;
        } else {
            String mode = generalPreferences.getString(GeneralKeys.KEY_FORM_UPDATE_MODE);
            return FormUpdateMode.parse(context, mode);
        }
    }
}
