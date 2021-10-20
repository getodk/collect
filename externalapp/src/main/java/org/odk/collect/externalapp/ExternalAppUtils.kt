package org.odk.collect.externalapp

import android.app.Activity
import android.content.Intent

object ExternalAppUtils {

    @JvmStatic
    fun returnSingleValue(activity: Activity, value: String) {
        val intent = Intent()
        intent.putExtra("value", value)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

    @JvmStatic
    fun getReturnedSingleValue(data: Intent): String? {
        return data.getStringExtra("value")
    }
}
