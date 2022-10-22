package org.odk.collect.androidshared.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import org.odk.collect.androidshared.R
import org.odk.collect.androidshared.data.getState
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
        val preferences = getPreferences(context)

        return if (conditionFailure) {
            LayoutInflater.from(context).inflate(R.layout.crash_layout, null).also {
                it.findViewById<TextView>(R.id.title).setText(R.string.cant_start_app)
                it.findViewById<View>(R.id.ok_button).setOnClickListener { processKiller.run() }
            }
        } else if (preferences.getBoolean(KEY_CRASH, false)) {
            LayoutInflater.from(context).inflate(R.layout.crash_layout, null).also {
                it.findViewById<TextView>(R.id.title).setText(R.string.crash_last_run)
                it.findViewById<View>(R.id.ok_button).setOnClickListener {
                    preferences.edit().remove(KEY_CRASH).apply()
                    onErrorDismissed?.run()
                }
            }
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

            return crashHandler.also {
                context.getState().set("crash_handler", it)
            }
        }

        @JvmStatic
        fun getInstance(context: Context): CrashHandler? {
            return context.getState().get<CrashHandler>("crash_handler")
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
