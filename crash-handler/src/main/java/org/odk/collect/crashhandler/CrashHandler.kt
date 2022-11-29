package org.odk.collect.crashhandler

import android.content.Context
import android.content.SharedPreferences
import org.odk.collect.androidshared.data.getState
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

class CrashHandler(private val processKiller: Runnable = Runnable { exitProcess(0) }) {

    var createMockViews = false

    private var conditionFailure: String? = null

    fun launchApp(conditionsCheck: Runnable, onSuccess: Runnable? = null) {
        if (checkConditions(conditionsCheck)) {
            onSuccess?.run()
        }
    }

    fun registerCrash(context: Context, crash: Throwable) {
        getPreferences(context).edit().putString(KEY_CRASH, crash.message ?: "").apply()
    }

    fun hasCrashed(context: Context): Boolean {
        return getPreferences(context).contains(KEY_CRASH) || conditionFailure != null
    }

    @JvmOverloads
    fun getCrashView(context: Context, onErrorDismissed: Runnable? = null): CrashView? {
        val preferences = getPreferences(context)

        return if (conditionFailure != null) {
            val crashMessage = conditionFailure

            createCrashView(context).also {
                it.setCrash(context.getString(R.string.cant_start_app), crashMessage) {
                    processKiller.run()
                }
            }
        } else if (preferences.contains(KEY_CRASH)) {
            val crashMessage = preferences.getString(KEY_CRASH, null)

            createCrashView(context).also {
                it.setCrash(context.getString(R.string.crash_last_run), crashMessage) {
                    preferences.edit().remove(KEY_CRASH).apply()
                    onErrorDismissed?.run()
                }
            }
        } else {
            null
        }
    }

    private fun checkConditions(runnable: Runnable): Boolean {
        return try {
            runnable.run()
            true
        } catch (t: Throwable) {
            conditionFailure = t.message ?: ""
            false
        }
    }

    private fun createCrashView(context: Context): CrashView {
        return if (createMockViews) {
            MockCrashView(context)
        } else {
            CrashView(context)
        }
    }

    /**
     * Uses raw [SharedPreferences] instead of [Settings] to keep dependencies to a minimum.
     * [CrashHandler] might have to work in cases where dependency injection cannot be configured
     * due to an early crash or conditions check failing.
     */
    private fun getPreferences(context: Context) =
        context.getSharedPreferences(KEY_INSTANCE, Context.MODE_PRIVATE)

    companion object {

        private const val KEY_CRASH = "crash"
        private const val KEY_INSTANCE = "crash_handler"

        private var originalHandler: UncaughtExceptionHandler? = null

        @JvmStatic
        fun install(context: Context): CrashHandler {
            return CrashHandler().also {
                context.getState().set("crash_handler", it)
                wrapUncaughtExceptionHandler(it, context)
            }
        }

        @JvmStatic
        fun uninstall(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler)
            context.getState().set("crash_handler", null)
        }

        @JvmStatic
        fun getInstance(context: Context): CrashHandler? {
            return context.getState().get<CrashHandler>(KEY_INSTANCE)
        }

        private fun wrapUncaughtExceptionHandler(
            crashHandler: CrashHandler,
            context: Context,
        ) {
            if (originalHandler != null) {
                throw IllegalStateException("install() should not be called multiple times without uninstall()!")
            }

            val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler().also {
                originalHandler = it
            }

            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                crashHandler.registerCrash(context, e)
                defaultUncaughtExceptionHandler?.uncaughtException(t, e)
            }
        }
    }
}
