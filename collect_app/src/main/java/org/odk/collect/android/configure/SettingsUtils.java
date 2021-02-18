package org.odk.collect.android.configure;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.Protocol;

public class SettingsUtils {

    private SettingsUtils() {

    }

    @NonNull
    public static FormUpdateMode getFormUpdateMode(Context context, SharedPreferences generalSharedPreferences) {
        String protocol = generalSharedPreferences.getString(GeneralKeys.KEY_PROTOCOL, null);

        if (Protocol.parse(context, protocol) == Protocol.GOOGLE) {
            return FormUpdateMode.MANUAL;
        } else {
            String mode = generalSharedPreferences.getString(GeneralKeys.KEY_FORM_UPDATE_MODE, null);
            return FormUpdateMode.parse(context, mode);
        }
    }
}
