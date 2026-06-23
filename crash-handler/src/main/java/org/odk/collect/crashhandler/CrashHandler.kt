package org.odk.collect.crashhandler

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.odk.collect.androidshared.data.getState
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess
import androidx.core.content.edit

class CrashHandler {

    private var conditionFailure: String? = null

    fun launchApp(conditionsCheck: Runnable, onSuccess: Runnable? = null) {
        if (checkConditions(conditionsCheck)) {
            onSuccess?.run()
        }
    }

    fun registerCrash(context: Context, crash: Throwable) {
        getPreferences(context).edit { putString(KEY_CRASH, crash.toJsonCrash()) }
    }

    fun hasCrashed(context: Context): Boolean {
        return getPreferences(context).contains(KEY_CRASH) || conditionFailure != null
    }

    fun getCrash(context: Context): Crash? {
        return conditionFailure.let {
            if (it != null) {
                Crash.ConditionFailure(it)
            } else {
                getPreferences(context).getString(KEY_CRASH, null).parseCrash()
            }
        }
    }

    fun dismissCrash(context: Context) {
        getPreferences(context).edit { remove(KEY_CRASH) }
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

        is Crash.Normal, is Crash.OutOfMemory -> {
            val message = if (crash is Crash.Normal) {
                crash.message
            } else {
                context.getString(org.odk.collect.strings.R.string.crash_oom_description)
            }

            CrashView(context).also {
                it.setCrash(
                    context.getString(org.odk.collect.strings.R.string.crash_last_run),
                    message
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
    class ConditionFailure(val message: String) : Crash()
    class Normal(val message: String) : Crash()
    object OutOfMemory : Crash()
}

private data class JsonCrash(val outOfMemory: Boolean, val message: String)

private fun Throwable.toJsonCrash(): String? {
    return Gson().toJson(
        JsonCrash(
            outOfMemory = this is OutOfMemoryError,
            message = this.message ?: ""
        )
    )
}

private fun String?.parseCrash(): Crash? {
    return if (this != null) {
        val jsonCrash = Gson().fromJson(this, JsonCrash::class.java)
        if (jsonCrash.outOfMemory) {
            Crash.OutOfMemory
        } else {
            Crash.Normal(jsonCrash.message)
        }
    } else {
        null
    }
}
