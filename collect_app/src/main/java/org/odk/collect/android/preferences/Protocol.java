package org.odk.collect.android.preferences;

import android.content.Context;

import org.odk.collect.android.R;

public enum Protocol {

    ODK(R.string.protocol_odk_default),
    GOOGLE(R.string.protocol_google_sheets);

    private final int string;

    Protocol(int string) {
        this.string = string;
    }

    public static Protocol parse(Context context, String value) {
        if (GOOGLE.getValue(context).equals(value))  {
            return GOOGLE;
        } else {
            return ODK;
        }
    }

    public String getValue(Context context) {
        return context.getString(string);
    }
}
