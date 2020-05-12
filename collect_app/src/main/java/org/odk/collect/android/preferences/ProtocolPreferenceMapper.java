package org.odk.collect.android.preferences;

import android.content.Context;

import org.odk.collect.android.R;

class ProtocolPreferenceMapper {

    private final Context context;

    ProtocolPreferenceMapper(Context context) {
        this.context = context;
    }

    Protocol getProtocol(String preferenceValue) {
        if (context.getString(R.string.protocol_google_sheets).equals(preferenceValue)) {
            return Protocol.GOOGLE;
        } else {
            return Protocol.ODK;
        }
    }

    public enum Protocol {
        ODK,
        GOOGLE
    }
}
