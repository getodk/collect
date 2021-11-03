package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ExternalAppIntentProvider
import org.odk.collect.androidshared.system.IntentLauncher
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowToast
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class FileRequesterImplTest {
    private val intentLauncher = spy(FakeIntentLauncher())
    private val requestCode = 99
    private val externalAppIntentProvider = mock<ExternalAppIntentProvider>()
    private val formEntryPrompt = mock<FormEntryPrompt>()
    private val availableIntent = Intent()
    private val unAvailableIntent = Intent().also {
        it.putExtra("fail", "fail")
    }

    private lateinit var activity: Activity
    private lateinit var fileRequester: FileRequester

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).get()
        fileRequester = FileRequesterImpl(intentLauncher, externalAppIntentProvider)
    }

    @Test
    fun `When exception is thrown by ExternalAppIntentProvider#getIntentToRunExternalApp a toast should be displayed`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).then {
            throw Exception("exception")
        }
        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )
        val toastText = ShadowToast.getTextOfLatestToast()
        assertThat(toastText, `is`("exception"))
    }

    @Test
    fun `When exception is thrown by ExternalAppIntentProvider#getIntentToRunExternalAppWithoutDefaultCategory a toast should be displayed`() {
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).then {
            throw Exception("exception")
        }
        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )
        val toastText = ShadowToast.getTextOfLatestToast()
        assertThat(toastText, `is`("exception"))
    }

    @Test
    fun `When error is thrown by ExternalAppIntentProvider#getIntentToRunExternalApp a toast should be displayed`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).then {
            throw Exception("error")
        }
        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )
        val toastText = ShadowToast.getTextOfLatestToast()
        assertThat(toastText, `is`("error"))
    }

    @Test
    fun `When error is thrown by ExternalAppIntentProvider#getIntentToRunExternalAppWithoutDefaultCategory a toast should be displayed`() {
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).then {
            throw Exception("error")
        }
        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )
        val toastText = ShadowToast.getTextOfLatestToast()
        assertThat(toastText, `is`("error"))
    }

    @Test
    fun `If the first attempt to start activity succeeded nothing else should happen`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            availableIntent
        )

        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )
        assertThat(intentLauncher.callCounter, `is`(1))
        assertThat(intentLauncher.errorCounter, `is`(0))
    }

    @Test
    fun `If the first attempt to start activity failed there should be another one`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(availableIntent)

        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )

        assertThat(intentLauncher.callCounter, `is`(2))
        assertThat(intentLauncher.errorCounter, `is`(1))
    }

    @Test
    fun `If both attempts to start activity failed a toast should be displayed`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(unAvailableIntent)

        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )

        assertThat(intentLauncher.callCounter, `is`(2))
        assertThat(intentLauncher.errorCounter, `is`(2))

        val toastText = ShadowToast.getTextOfLatestToast()
        assertThat(toastText, `is`(activity.getString(R.string.no_app)))
    }

    @Test
    fun `If both attempts to start activity failed a toast with custom message should be displayed if it is set`() {
        whenever(formEntryPrompt.getSpecialFormQuestionText("noAppErrorString")).thenReturn("Custom message")
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(unAvailableIntent)

        fileRequester.launch(
            activity,
            requestCode,
            formEntryPrompt
        )

        val toastText = ShadowToast.getTextOfLatestToast()
        assertThat(toastText, `is`("Custom message"))
    }

    class FakeIntentLauncher : IntentLauncher {
        var callCounter = 0
        var errorCounter = 0

        override fun launch(context: Context, intent: Intent?, onError: () -> Unit) {
        }

        override fun launchForResult(
            activity: Activity,
            intent: Intent?,
            requestCode: Int,
            onError: () -> Unit
        ) {
            callCounter++
            if (intent!!.hasExtra("fail")) {
                errorCounter++
                onError()
            }
        }

        override fun launchForResult(
            resultLauncher: ActivityResultLauncher<Intent>,
            intent: Intent?,
            onError: () -> Unit
        ) {
        }
    }
}
