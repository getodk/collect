package org.odk.collect.androidshared.utils

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
}
