package org.odk.collect.android.preferences;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_MIGRATED;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_USERNAME;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;

/**
 * Migrates existing preference values to metadata
 */
public class FormMetadataMigrator {

    /**
     * The migration flow, from source to target
     */
    static final String[][] SOURCE_TARGET_VALUE_PAIRS = new String[][]{
            {KEY_USERNAME, KEY_METADATA_USERNAME},
            {KEY_SELECTED_GOOGLE_ACCOUNT, KEY_METADATA_EMAIL}
    };

    private FormMetadataMigrator() {

    }

    /**
     * Migrates the form metadata if it hasn’t already been done
     */
    public static void migrate(GeneralSharedPreferences generalSharedPreferences) {
        boolean migrationAlreadyDone = generalSharedPreferences.getBoolean(KEY_METADATA_MIGRATED, false);
        Timber.i("migrate called, %s",
                (migrationAlreadyDone ? "migration already done" : "will migrate"));

        if (!migrationAlreadyDone) {
            for (String[] pair : SOURCE_TARGET_VALUE_PAIRS) {
                String migratingValue = ((String) generalSharedPreferences.get(pair[0])).trim();
                if (!migratingValue.isEmpty()) {
                    Timber.i("Copying %s from %s to %s", migratingValue, pair[0], pair[1]);
                    generalSharedPreferences.save(pair[1], migratingValue);
                }
            }

            // Save that we’ve migrated the values
            generalSharedPreferences.save(KEY_METADATA_MIGRATED, true);
        }
    }
}
