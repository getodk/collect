package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import org.odk.collect.android.application.Collect;

import java.util.Arrays;

public class PrefUtils {
    private PrefUtils() { }  // prevent instantiation of this utility class

    public static SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(
            Collect.getInstance().getApplicationContext());
    }

    public static ListPreference createListPref(
        Context context, String key, int titleId, int labelsId, int valuesId) {
        Resources resources = context.getResources();
        return createListPref(context, key, titleId,
            resources.getStringArray(labelsId), resources.getStringArray(valuesId));
    }

    public static ListPreference createListPref(
        Context context, String key, int titleId, int[] labelIds, String[] values) {
        Resources resources = context.getResources();
        String[] labels = new String[labelIds.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = resources.getString(labelIds[i]);
        }
        return createListPref(context, key, titleId, labels, values);
    }

    public static ListPreference createListPref(
        Context context, String key, int titleId, String[] labels, String[] values) {
        ensurePrefHasValidValue(key, values);
        ListPreference pref = new ListPreference(context);
        pref.setKey(key);
        pref.setPersistent(true);
        pref.setTitle(titleId);
        pref.setDialogTitle(titleId);
        pref.setEntries(labels);
        pref.setEntryValues(values);
        pref.setSummary("%s");
        return pref;
    }

    public static void ensurePrefHasValidValue(String key, String[] validValues) {
        SharedPreferences prefs = getSharedPrefs();
        String value = prefs.getString(key, null);
        if (Arrays.asList(validValues).indexOf(value) < 0) {
            if (validValues.length > 0) {
                prefs.edit().putString(key, validValues[0]).apply();
            } else {
                prefs.edit().remove(key).apply();
            }
        }
    }

    /**
     * Gets an integer value from the shared preferences.  If the preference has
     * a string value, attempts to convert it to an integer.  If the preference
     * is not found or is not a valid integer, returns the defaultValue.
     */
    public static int getInt(String key, int defaultValue) {
        Object value = getSharedPrefs().getAll().get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) { /* ignore */ }
        }
        return defaultValue;
    }
}
