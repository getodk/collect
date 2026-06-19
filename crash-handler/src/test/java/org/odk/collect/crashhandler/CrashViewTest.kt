package org.odk.collect.crashhandler

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class CrashViewTest {

    private val context = ApplicationProvider.getApplicationContext<Context>().also {
        it.setTheme(com.google.android.material.R.style.Theme_MaterialComponents)
    }

    private val processKiller = mock<Runnable>()

    @Test
    fun getCrashView_whenThereAreNoCrashesRegistered_returnsNull() {
        assertThat(getCrashView(createCrashHandler(), context), equalTo(null))
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_returnsViewWithCrashDetails() {
        val crashHandler = createCrashHandler()
        crashHandler.registerCrash(context, RuntimeException("crash!"))

        val view = getCrashView(crashHandler, context)
        assertThat(
            view!!.findViewById<TextView>(R.id.title).text,
            equalTo(context.getString(org.odk.collect.strings.R.string.crash_last_run))
        )
        assertThat(view.findViewById<TextView>(R.id.message).text, equalTo("crash!"))
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andTheOkButtonIsClickedOnTheView_runsTheOnErrorDismissedListener() {
        val crashHandler = createCrashHandler()
        crashHandler.registerCrash(context, RuntimeException())

        val onErrorDismissed = mock<Runnable>()
        val view = getCrashView(crashHandler, context, onErrorDismissed = onErrorDismissed)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(onErrorDismissed).run()
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andTheOkButtonIsClickedOnTheView_doesNotRunProcessKiller() {
        val crashHandler = createCrashHandler()
        crashHandler.registerCrash(context, RuntimeException())

        val onErrorDismissed = mock<Runnable>()
        val view = getCrashView(crashHandler, context, onErrorDismissed)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(processKiller, never()).run()
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andTheOkButtonIsClickedOnTheView_clearsCrash() {
        val crashHandler = createCrashHandler()
        crashHandler.registerCrash(context, RuntimeException())

        // Check crash view is returned for multiple instances (for rotates etc)
        assertThat(getCrashView(crashHandler, context), notNullValue())

        val onErrorDismissed = mock<Runnable>()
        val view = getCrashView(crashHandler, context, onErrorDismissed)

        view!!.findViewById<View>(R.id.ok_button).performClick()
        assertThat(getCrashView(crashHandler, context), equalTo(null))
    }

    @Test
    fun getCrashView_whenCheckingConditionsFails_returnsViewWithCrashDetails() {
        val crashHandler = createCrashHandler()

        crashHandler.launchApp({
            throw RuntimeException("blah")
        })

        val view = getCrashView(crashHandler, context)
        assertThat(
            view!!.findViewById<TextView>(R.id.title).text,
            equalTo(context.getString(org.odk.collect.strings.R.string.cant_start_app))
        )
        assertThat(view.findViewById<TextView>(R.id.message).text, equalTo("blah"))
    }

    @Test
    fun getCrashView_whenCheckingConditionsFails_andTheOkButtonIsClickedOnTheView_killsTheProcess() {
        val crashHandler = createCrashHandler()

        crashHandler.launchApp({
            throw RuntimeException("blah")
        })

        val view = getCrashView(crashHandler, context, processKiller = processKiller)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(processKiller).run()
    }

    @Test
    fun getCrashView_whenCheckingConditionsFails_andTheOkButtonIsClickedOnTheView_doesNotRunErrorDismissedListener() {
        val crashHandler = createCrashHandler()

        crashHandler.launchApp({
            throw RuntimeException("blah")
        })

        val onErrorDismissed = mock<Runnable>()
        val view = getCrashView(
            crashHandler,
            context,
            processKiller = processKiller,
            onErrorDismissed = onErrorDismissed
        )
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(onErrorDismissed, never()).run()
    }

    @Test
    fun getCrashView_whenCheckConditionFailedInDifferentInstance_returnsNull() {
        val crashHandler = createCrashHandler()
        crashHandler.launchApp({
            throw RuntimeException("blah")
        })

        val otherCrashHandler = createCrashHandler()
        assertThat(getCrashView(otherCrashHandler, context), equalTo(null))
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andCheckConditionFailed_andTheOkButtonIsClickedOnTheView_runsProcessKiller() {
        val crashHandler = createCrashHandler()
        crashHandler.registerCrash(context, RuntimeException())

        crashHandler.launchApp({
            throw RuntimeException("blah")
        })

        val view = getCrashView(crashHandler, context, processKiller = processKiller)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(processKiller).run()
    }

    private fun createCrashHandler() = CrashHandler(processKiller)
}