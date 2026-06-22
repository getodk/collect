package org.odk.collect.crashhandler

import android.content.Context
import android.content.SharedPreferences
import org.odk.collect.androidshared.data.getState
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

class CrashHandler {

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

    fun getCrash(context: Context): Crash? {
        return conditionFailure.let {
            if (it != null) {
                Crash.ConditionFailure(it)
            } else {
                val preferences = getPreferences(context)
                val crashMessage = preferences.getString(KEY_CRASH, null)
                if (crashMessage != null) {
                    Crash.Normal(crashMessage)
                } else {
                    null
                }
            }
        }
    }

    fun dismissCrash(context: Context) {
        getPreferences(context).edit().remove(KEY_CRASH).apply()
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
            context.getState().set("crash_handler", null)
            unwrapUncaughtExceptionHandler()
        }

        @JvmStatic
        fun getInstance(context: Context): CrashHandler? {
            return context.getState().get<CrashHandler>(KEY_INSTANCE)
        }

        private fun wrapUncaughtExceptionHandler(
            crashHandler: CrashHandler,
            context: Context
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

        private fun unwrapUncaughtExceptionHandler() {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler)
            originalHandler = null
        }
    }
}

@JvmOverloads
fun getCrashView(
    crashHandler: CrashHandler,
    context: Context,
    processKiller: Runnable = Runnable { exitProcess(0) },
    onErrorDismissed: Runnable? = null
): CrashView? {
    return when (val crash = crashHandler.getCrash(context)) {
        is Crash.ConditionFailure -> {
            CrashView(context).also {
                it.setCrash(
                    context.getString(org.odk.collect.strings.R.string.cant_start_app),
                    crash.message
                ) {
                    processKiller.run()
                }
            }
        }

        is Crash.Normal -> {
            CrashView(context).also {
                it.setCrash(
                    context.getString(org.odk.collect.strings.R.string.crash_last_run),
                    crash.message
                ) {
                    crashHandler.dismissCrash(context)
                    onErrorDismissed?.run()
                }
            }
        }

        null -> null
    }
}

sealed class Crash {
    data class ConditionFailure(val message: String) : Crash()
    data class Normal(val message: String) : Crash()
}
