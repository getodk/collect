package org.odk.collect.android.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.preferences.FormMetadataMigrator.migrateOnce;
import static org.odk.collect.android.preferences.FormMetadataMigrator.sourceTargetValuePairs;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_MIGRATED;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_PHONENUMBER;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_METADATA_USERNAME;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;

/** Tests the FormMetadataActivity */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class FormMetadataMigratorTest {

    private FormMetadataActivity formMetadataActivity;
    private SharedPreferences sharedPreferences;
    private final PrintStream o = System.out;

    /** The keys of preferences affected by the migration */
    private final List<String> affectedKeys = Arrays.asList(
            KEY_METADATA_MIGRATED,
            KEY_METADATA_USERNAME,
            KEY_METADATA_PHONENUMBER,
            KEY_METADATA_EMAIL,
            KEY_USERNAME,
            KEY_SELECTED_GOOGLE_ACCOUNT);

    /** The inputs to the migration */
    private final String[][] sourceKeyValuePairs = new String[][] {
            {KEY_USERNAME,                  "a user"},
            {KEY_SELECTED_GOOGLE_ACCOUNT,   "a Google email address"}
    };

    /** Changes to make to the metadata after the migration */
    private final String[][] modifiedMetadataValuePairs = new String[][] {
            {KEY_METADATA_USERNAME,         "a user--changed"},
            {KEY_METADATA_PHONENUMBER,      "a phone number--changed"},
            {KEY_METADATA_EMAIL,            "an email--changed"},
    };

    @Before
    public void setUp() throws Exception {
        formMetadataActivity = Robolectric.setupActivity(FormMetadataActivity.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(formMetadataActivity);
    }

    @Test
    public void shouldMigrateDataCorrectly() {
        setPreferencesToPreMigrationValues();
        displayAffectedPreferences("Before calling migrateOnce");

        migrateOnce(sharedPreferences);
        displayAffectedPreferences("After calling migrateOnce");
        checkPostMigrationValues();

        setPreferencesToValues(modifiedMetadataValuePairs);
        displayAffectedPreferences("After changing metadata");
        migrateOnce(sharedPreferences);
        displayAffectedPreferences("After calling migrateOnce again");
        ensureSecondMigrationCallPreservesMetadata();
    }

    private void displayAffectedPreferences(String message) {
        o.println("\n" + message);
        SortedMap<String, ?> allPrefs = new TreeMap<>(sharedPreferences.getAll());
        for (Map.Entry<String, ?> es : allPrefs.entrySet()) {
            if (affectedKeys.contains(es.getKey())) {
                o.format("%-25s %s\n", es.getKey(), es.getValue());
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private void setPreferencesToPreMigrationValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_METADATA_MIGRATED, false);
        editor.commit();

        setPreferencesToValues(sourceKeyValuePairs);
    }

    @SuppressLint("ApplySharedPref")
    private void setPreferencesToValues(String[][] valuePairs) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String[] pair : valuePairs) {
            editor.putString(pair[0], pair[1]);
        }
        editor.commit();
    }

    private void checkPostMigrationValues() {
        assertTrue(sharedPreferences.getBoolean(KEY_METADATA_MIGRATED, false));
        assertPrefsMatchValues(sourceKeyValuePairs);

        for (String[] pair : sourceTargetValuePairs) {
            assertEquals(sharedPreferences.getString(pair[0], ""),
                    sharedPreferences.getString(pair[1], ""));
        }
    }

    private void ensureSecondMigrationCallPreservesMetadata() {
        assertPrefsMatchValues(modifiedMetadataValuePairs);
    }

    private void assertPrefsMatchValues(String[][] valuePairs) {
        for (String[] pair : valuePairs) {
            String prefValue = sharedPreferences.getString(pair[0], "");
            assertEquals(prefValue, pair[1]);
        }
    }
}
