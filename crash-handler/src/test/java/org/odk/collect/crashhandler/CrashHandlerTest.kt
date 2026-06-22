package org.odk.collect.crashhandler

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class CrashHandlerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>().also {
        it.setTheme(com.google.android.material.R.style.Theme_MaterialComponents)
    }

    @Test
    fun hasCrashed_whenThereAreNoCrashesRegistered_andNoConditionsFailed_returnsFalse() {
        val crashHandler = createCrashHandler()
        crashHandler.launchApp({})
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
        crashHandler.launchApp({ throw RuntimeException() })
        assertThat(crashHandler.hasCrashed(context), equalTo(true))
    }

    @Test
    fun launchApp_whenConditionsFail_runsOnSuccess() {
        val crashHandler = createCrashHandler()

        val onSuccess = mock<Runnable>()
        crashHandler.launchApp(conditionsCheck = { }, onSuccess = onSuccess)
        verify(onSuccess).run()
    }

    @Test
    fun launchApp_whenSomethingFails_doesNotRunOnSuccess() {
        val crashHandler = createCrashHandler()

        val onSuccess = mock<Runnable>()
        crashHandler.launchApp(
            conditionsCheck = { throw RuntimeException() },
            onSuccess = onSuccess
        )
        verify(onSuccess, never()).run()
    }

    private fun createCrashHandler() = CrashHandler()
}
