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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ExternalAppIntentProvider
import org.odk.collect.androidshared.system.IntentLauncher
import org.robolectric.Robolectric
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class StringRequesterImplTest {
    private val intentLauncher = spy(FakeIntentLauncher())
    private val requestCode = 99
    private val externalAppIntentProvider = mock<ExternalAppIntentProvider>()
    private val formEntryPrompt = mock<FormEntryPrompt>()
    private val onError: (String) -> Unit = mock()
    private val availableActionSendToIntent = Intent(Intent.ACTION_SENDTO)
    private val unAvailableActionSendToIntent = Intent(Intent.ACTION_SENDTO).also {
        it.putExtra("fail", "fail")
    }
    private val availableIntent = Intent()
    private val unAvailableIntent = Intent().also {
        it.putExtra("fail", "fail")
    }

    private lateinit var activity: Activity
    private lateinit var stringRequester: StringRequester

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).get()
        stringRequester = StringRequesterImpl(intentLauncher, externalAppIntentProvider)
    }

    @Test
    fun `When exception is thrown by ExternalAppIntentProvider#getIntentToRunExternalApp onError should be called`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).then {
            throw Exception("exception")
        }
        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        verify(onError).invoke("exception")
    }

    @Test
    fun `When exception is thrown by ExternalAppIntentProvider#getIntentToRunExternalAppWithoutDefaultCategory onError should be called`() {
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).then {
            throw Exception("exception")
        }
        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        verify(onError).invoke("exception")
    }

    @Test
    fun `When error is thrown by ExternalAppIntentProvider#getIntentToRunExternalApp onError should be called`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).then {
            throw Exception("error")
        }
        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        verify(onError).invoke("error")
    }

    @Test
    fun `When error is thrown by ExternalAppIntentProvider#getIntentToRunExternalAppWithoutDefaultCategory onError should be called`() {
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).then {
            throw Exception("error")
        }
        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        verify(onError).invoke("error")
    }

    @Test
    fun `If the first attempt to start activity succeeded nothing else should happen`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            availableIntent
        )

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        assertThat(intentLauncher.launchCallCounter, `is`(0))
        assertThat(intentLauncher.launchForResultCallCounter, `is`(1))
        assertThat(intentLauncher.errorCounter, `is`(0))
    }

    @Test
    fun `If the first attempt to start activity succeeded for intent with ACTION_SENDTO nothing else should happen`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            availableActionSendToIntent
        )

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        assertThat(intentLauncher.launchCallCounter, `is`(1))
        assertThat(intentLauncher.launchForResultCallCounter, `is`(0))
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

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        assertThat(intentLauncher.launchCallCounter, `is`(0))
        assertThat(intentLauncher.launchForResultCallCounter, `is`(2))
        assertThat(intentLauncher.errorCounter, `is`(1))
    }

    @Test
    fun `If the first attempt to start activity failed for intent with ACTION_SENDTO there should be another one`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableActionSendToIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(availableIntent)

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        assertThat(intentLauncher.launchCallCounter, `is`(2))
        assertThat(intentLauncher.launchForResultCallCounter, `is`(0))
        assertThat(intentLauncher.errorCounter, `is`(1))
    }

    @Test
    fun `If both attempts to start activity failed onError should be called`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(unAvailableIntent)

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        assertThat(intentLauncher.launchCallCounter, `is`(0))
        assertThat(intentLauncher.launchForResultCallCounter, `is`(2))
        assertThat(intentLauncher.errorCounter, `is`(2))

        verify(onError).invoke(activity.getString(R.string.no_app))
    }

    @Test
    fun `If both attempts to start activity failed for intent with ACTION_SENDTO onError should be called`() {
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableActionSendToIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(unAvailableActionSendToIntent)

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        assertThat(intentLauncher.launchCallCounter, `is`(2))
        assertThat(intentLauncher.launchForResultCallCounter, `is`(0))
        assertThat(intentLauncher.errorCounter, `is`(2))

        verify(onError).invoke(activity.getString(R.string.no_app))
    }

    @Test
    fun `If both attempts to start activity failed onError should be called with custom message if it is set`() {
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

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        verify(onError).invoke("Custom message")
    }

    @Test
    fun `If both attempts to start activity failed for intent with ACTION_SENDTO onError should be called with custom message if it is set`() {
        whenever(formEntryPrompt.getSpecialFormQuestionText("noAppErrorString")).thenReturn("Custom message")
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableActionSendToIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(unAvailableActionSendToIntent)

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            null,
            onError
        )

        verify(onError).invoke("Custom message")
    }

    @Test
    fun `Value should be added to intent`() {
        whenever(formEntryPrompt.getSpecialFormQuestionText("noAppErrorString")).thenReturn("Custom message")
        whenever(externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)).thenReturn(
            unAvailableIntent
        )
        whenever(
            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                formEntryPrompt,
                activity.packageManager
            )
        ).thenReturn(availableIntent)

        stringRequester.launch(
            activity,
            requestCode,
            formEntryPrompt,
            "123",
            onError
        )

        assertThat(unAvailableIntent.getSerializableExtra("value"), `is`("123"))
        assertThat(availableIntent.getSerializableExtra("value"), `is`("123"))
    }

    class FakeIntentLauncher : IntentLauncher {
        var launchCallCounter = 0
        var launchForResultCallCounter = 0
        var errorCounter = 0

        override fun launch(context: Context, intent: Intent?, onError: () -> Unit) {
            launchCallCounter++
            if (intent!!.hasExtra("fail")) {
                errorCounter++
                onError()
            }
        }

        override fun launchForResult(
            activity: Activity,
            intent: Intent?,
            requestCode: Int,
            onError: () -> Unit
        ) {
            launchForResultCallCounter++
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
