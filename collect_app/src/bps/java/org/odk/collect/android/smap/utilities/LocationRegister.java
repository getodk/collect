package org.odk.collect.android.smap.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import org.odk.collect.android.database.TraceUtilities;
import org.odk.collect.android.preferences.GeneralKeys;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

public class LocationRegister {

    public boolean locationEnabled() {
        return false;
    }

    public boolean taskLocationEnabled() {
        return false;
    }

    public void register(Context context, Location location) {
        // Do nothing
    }

    /*
     * Disable permissions concerned with background location
     */
    public void set(SharedPreferences.Editor editor, String sendLocation) {
        editor.putBoolean(GeneralKeys.KEY_SMAP_USER_LOCATION, false);
        editor.putBoolean(GeneralKeys.KEY_SMAP_OVERRIDE_LOCATION, true);
    }
}
