package org.odk.collect.externalapp

import android.app.Activity
import android.content.Intent

/**
 * Helpers for returning answers to [FormEntryActivity] via [Activity.onActivityResult] and also
 * for dealing with those answers in [Activity.onActivityResult] itself.
 */
object ExternalAppUtils {

    private const val VALUE_KEY = "value"

    @JvmStatic
    fun returnSingleValue(activity: Activity, value: String) {
        val intent = Intent()
        intent.putExtra(VALUE_KEY, value)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

    @JvmStatic
    fun getReturnedSingleValue(data: Intent): String? {
        return data.getStringExtra(VALUE_KEY)
    }
}
