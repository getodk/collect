package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import java.util.Arrays;

public class PrefUtils {
    private PrefUtils() { }  // prevent instantiation of this utility class

    public static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
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
        ensurePrefHasValidValue(context, key, values);
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

    public static void ensurePrefHasValidValue(Context context, String key, String[] values) {
        SharedPreferences prefs = getSharedPrefs(context);
        String value = prefs.getString(key, null);
        if (Arrays.asList(values).indexOf(value) < 0) {
            if (values.length > 0) {
                prefs.edit().putString(key, values[0]).apply();
            } else {
                prefs.edit().remove(key).apply();
            }
        }
    }

}
