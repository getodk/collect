package org.odk.collect.android.preferences;

import android.os.Bundle;

import androidx.preference.ListPreferenceDialogFragmentCompat;

public class ReferenceLayerPreferenceDialog extends ListPreferenceDialogFragmentCompat {

    public static ReferenceLayerPreferenceDialog newInstance(String key) {
        ReferenceLayerPreferenceDialog fragment = new ReferenceLayerPreferenceDialog();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }
}
