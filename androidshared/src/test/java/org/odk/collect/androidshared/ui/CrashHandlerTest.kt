package org.odk.collect.androidshared.ui

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
import org.odk.collect.androidshared.R

@RunWith(AndroidJUnit4::class)
class CrashHandlerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>().also {
        it.setTheme(R.style.Theme_MaterialComponents)
    }

    private val processKiller = mock<Runnable>()

    @Test
    fun hasCrashed_whenThereAreNoCrashesRegistered_andNoConditionsFailed_returnsFalse() {
        val crashHandler = createCrashHandler()
        crashHandler.checkConditions { }
        assertThat(crashHandler.hasCrashed(context), equalTo(false))
    }

    @Test
    fun hasCrashed_whenThereHasBeenACrashRegistered_returnsTrue() {
        createCrashHandler().registerCrash(context, RuntimeException())
        assertThat(createCrashHandler().hasCrashed(context), equalTo(true))
    }

    @Test
    fun hasCrashed_whenCheckingConditionsFails_returnsTrue() {
        val crashHandler = createCrashHandler()
        crashHandler.checkConditions { throw RuntimeException() }
        assertThat(crashHandler.hasCrashed(context), equalTo(true))
    }

    @Test
    fun getCrashView_whenThereAreNoCrashesRegistered_returnsNull() {
        assertThat(createCrashHandler().getCrashView(context), equalTo(null))
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_returnsViewWithCrashDetails() {
        createCrashHandler().registerCrash(context, RuntimeException())

        val view = createCrashHandler().getCrashView(context)
        assertThat(view!!.findViewById<TextView>(R.id.title).text,
            equalTo(context.getString(R.string.crash_last_run)))
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andTheOkButtonIsClickedOnTheView_runsTheOnErrorDismissedListener() {
        createCrashHandler().registerCrash(context, RuntimeException())

        val onErrorDismissed = mock<Runnable>()
        val view = createCrashHandler().getCrashView(context, onErrorDismissed)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(onErrorDismissed).run()
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andTheOkButtonIsClickedOnTheView_doesNotRunProcessKiller() {
        createCrashHandler().registerCrash(context, RuntimeException())

        val onErrorDismissed = mock<Runnable>()
        val view = createCrashHandler().getCrashView(context, onErrorDismissed)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(processKiller, never()).run()
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andTheOkButtonIsClickedOnTheView_clearsCrash() {
        createCrashHandler().registerCrash(context, RuntimeException())

        // Check crash view is returned for multiple instances (for rotates etc)
        assertThat(createCrashHandler().getCrashView(context), notNullValue())

        val onErrorDismissed = mock<Runnable>()
        val view = createCrashHandler().getCrashView(context, onErrorDismissed)

        view!!.findViewById<View>(R.id.ok_button).performClick()
        assertThat(createCrashHandler().getCrashView(context), equalTo(null))
    }

    @Test
    fun getCrashView_whenCheckingConditionsFails_returnsViewWithCrashDetails() {
        val crashHandler = createCrashHandler()

        crashHandler.checkConditions {
            throw RuntimeException("blah")
        }

        val view = crashHandler.getCrashView(context)
        assertThat(view!!.findViewById<TextView>(R.id.title).text,
            equalTo(context.getString(R.string.cant_start_app)))
    }

    @Test
    fun getCrashView_whenCheckingConditionsFails_andTheOkButtonIsClickedOnTheView_killsTheProcess() {
        val crashHandler = createCrashHandler()

        crashHandler.checkConditions {
            throw RuntimeException("blah")
        }

        val view = crashHandler.getCrashView(context)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(processKiller).run()
    }

    @Test
    fun getCrashView_whenCheckingConditionsFails_andTheOkButtonIsClickedOnTheView_doesNotRunErrorDismissedListener() {
        val crashHandler = createCrashHandler()

        crashHandler.checkConditions {
            throw RuntimeException("blah")
        }

        val onErrorDismissed = mock<Runnable>()
        val view = crashHandler.getCrashView(context, onErrorDismissed)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(onErrorDismissed, never()).run()
    }

    @Test
    fun getCrashView_whenCheckConditionFailedInDifferentInstance_returnsNull() {
        val crashHandler = createCrashHandler()
        crashHandler.checkConditions {
            throw RuntimeException("blah")
        }

        val otherCrashHandler = createCrashHandler()
        assertThat(otherCrashHandler.getCrashView(context), equalTo(null))
    }

    @Test
    fun getCrashView_whenThereHasBeenACrashRegistered_andCheckConditionFailed_andTheOkButtonIsClickedOnTheView_runsProcessKiller() {
        createCrashHandler().registerCrash(context, RuntimeException())

        val crashHandler = createCrashHandler()
        crashHandler.checkConditions {
            throw RuntimeException("blah")
        }

        val view = crashHandler.getCrashView(context)
        view!!.findViewById<View>(R.id.ok_button).performClick()
        verify(processKiller).run()
    }

    @Test
    fun checkConditions_whenNothingFails_returnsTrue() {
        val crashHandler = createCrashHandler()
        assertThat(crashHandler.checkConditions { }, equalTo(true))
    }

    @Test
    fun checkConditions_whenSomethingFails_returnsFalse() {
        val crashHandler = createCrashHandler()
        assertThat(crashHandler.checkConditions { throw RuntimeException() }, equalTo(false))
    }

    private fun createCrashHandler() = CrashHandler(processKiller)
}
