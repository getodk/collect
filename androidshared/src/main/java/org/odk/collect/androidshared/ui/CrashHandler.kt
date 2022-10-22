package org.odk.collect.androidshared.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import org.odk.collect.androidshared.R
import kotlin.system.exitProcess

class CrashHandler(private val processKiller: Runnable = Runnable { exitProcess(0) }) {

    private var conditionFailure = false

    fun registerCrash(context: Context, crash: Throwable) {
        getPreferences(context).edit().putBoolean(KEY_CRASH, true).apply()
    }

    fun checkConditions(runnable: Runnable) {
        try {
            runnable.run()
        } catch (t: Throwable) {
            conditionFailure = true
        }
    }

    fun getCrashView(context: Context, onErrorDismissed: Runnable? = null): View? {
        val crash = getPreferences(context).getBoolean(KEY_CRASH, false)

        return if (crash || conditionFailure) {
            val view = LayoutInflater.from(context).inflate(R.layout.crash_layout, null)

            if (conditionFailure) {
                view.findViewById<TextView>(R.id.title).setText(R.string.cant_start_app)
            } else {
                view.findViewById<TextView>(R.id.title).setText(R.string.crash_last_run)
            }

            view.findViewById<View>(R.id.ok_button).setOnClickListener {
                getPreferences(context).edit().remove(KEY_CRASH).apply()

                if (conditionFailure) {
                    processKiller.run()
                } else {
                    onErrorDismissed?.run()
                }
            }
            view
        } else {
            null
        }
    }

    private fun getPreferences(context: Context) =
        context.getSharedPreferences("crash_handler", Context.MODE_PRIVATE)

    companion object {

        private const val KEY_CRASH = "crash"

        @JvmStatic
        fun install(context: Context): CrashHandler {
            val crashHandler = CrashHandler()
            wrapUncaughExceptionHandler(crashHandler, context)

            return crashHandler
        }

        private fun wrapUncaughExceptionHandler(
            crashHandler: CrashHandler,
            context: Context,
        ) {
            val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                crashHandler.registerCrash(context, e)
                defaultUncaughtExceptionHandler?.uncaughtException(t, e)
            }
        }
    }
}
