package org.odk.collect.android.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_MIGRATED;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_USERNAME;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;

/** Migrates existing preference values to metadata */
public class FormMetadataMigrator {

    private FormMetadataMigrator() {

    }

    /** The migration flow, from source to target */
    static final String[][] SOURCE_TARGET_VALUE_PAIRS = new String[][]{
            {KEY_USERNAME,                  KEY_METADATA_USERNAME},
            {KEY_SELECTED_GOOGLE_ACCOUNT,   KEY_METADATA_EMAIL}
    };

    /** Migrates the form metadata if it hasn’t already been done */
    @SuppressLint("ApplySharedPref")
    public static void migrate(SharedPreferences sharedPreferences) {
        boolean migrationAlreadyDone = sharedPreferences.getBoolean(KEY_METADATA_MIGRATED, false);
        Timber.i("migrate called, %s",
                (migrationAlreadyDone ? "migration already done" : "will migrate"));

        if (! migrationAlreadyDone) {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String[] pair : SOURCE_TARGET_VALUE_PAIRS) {
                String migratingValue = sharedPreferences.getString(pair[0], "").trim();
                if (! migratingValue.isEmpty()) {
                    Timber.i("Copying %s from %s to %s", migratingValue, pair[0], pair[1]);
                    editor.putString(pair[1], migratingValue);
                }
            }

            // Save that we’ve migrated the values
            editor.putBoolean(KEY_METADATA_MIGRATED, true);
            editor.commit();
        }
    }
}
