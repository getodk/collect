package org.odk.collect.androidshared.utils

import android.app.Activity
import android.content.Context
import android.content.Intent

object IntentLauncher {
    fun launch(context: Context, intent: Intent, onError: () -> Unit) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            onError()
        } catch (e: Error) {
            onError()
        }
    }

    fun launchForResult(activity: Activity, intent: Intent, requestCode: Int, onError: () -> Unit) {
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            onError()
        } catch (e: Error) {
            onError()
        }
    }
}
