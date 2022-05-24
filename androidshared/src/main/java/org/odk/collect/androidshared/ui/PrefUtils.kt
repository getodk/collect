package org.odk.collect.androidshared.ui

import android.content.Context
import androidx.preference.ListPreference
import org.odk.collect.shared.settings.Settings

object PrefUtils {

    @JvmStatic
    fun createListPref(
        context: Context,
        key: String,
        title: String,
        labelIds: IntArray,
        values: Array<String>,
        settings: Settings,
    ): ListPreference {
        val labels: Array<String?> = labelIds.map { context.getString(it) }.toTypedArray()
        return createListPref(context, key, title, labels, values, settings)
    }

    /**
     * Gets an integer value from the shared preferences. If the preference has
     * a string value, attempts to convert it to an integer. If the preference
     * is not found or is not a valid integer, returns the defaultValue.
     */
    @JvmStatic
    fun getInt(key: String?, defaultValue: Int, settings: Settings): Int {
        val value: Any? = settings.getAll()[key]
        if (value is Int) {
            return value
        }

        if (value is String) {
            try {
                return Integer.parseInt(value)
            } catch (e: NumberFormatException) {
                // ignore
            }
        }

        return defaultValue
    }

    private fun createListPref(
        context: Context,
        key: String,
        title: String,
        labels: Array<String?>,
        values: Array<String>,
        settings: Settings,
    ): ListPreference {
        ensurePrefHasValidValue(key, values, settings)
        return ListPreference(context).also {
            it.key = key
            it.isPersistent = true
            it.title = title
            it.dialogTitle = title
            it.entries = labels
            it.entryValues = values
            it.summary = "%s"
        }
    }

    private fun ensurePrefHasValidValue(
        key: String,
        validValues: Array<String>,
        settings: Settings,
    ) {
        val value = settings.getString(key)
        if (validValues.indexOf(value) < 0) {
            if (validValues.isNotEmpty()) {
                settings.save(key, validValues[0])
            } else {
                settings.remove(key)
            }
        }
    }
}
