package org.odk.collect.androidshared.ui

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

object CrashHandler {

    private const val KEY_CRASH = "crash"

    private var initializationFailed = false

    @JvmStatic
    fun initializeApp(context: Context, initializeApp: Runnable) {
        try {
            initializeApp.run()
        } catch (t: Throwable) {
            initializationFailed = true
        }

        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t: Thread, e: Throwable ->
            getPreferences(context).edit().putBoolean(KEY_CRASH, true).apply()
            defaultUncaughtExceptionHandler?.uncaughtException(t, e)
        }
    }

    @JvmStatic
    fun showCrashUIOrLaunchApp(context: Context, launchApp: Runnable) {
        val preferences = getPreferences(context)

        if (preferences.contains(KEY_CRASH)) {
            preferences.edit().remove(KEY_CRASH).apply()

            MaterialAlertDialogBuilder(context)
                .setTitle("Crashed!")
                .setOnDismissListener { launchApp.run() }
                .show()
        } else if (initializationFailed) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Crashed!")
                .setOnDismissListener { exitProcess(0) }
                .show()
        } else {
            launchApp.run()
        }
    }

    private fun getPreferences(context: Context) =
        context.getSharedPreferences("crash_handler", Context.MODE_PRIVATE)
}
