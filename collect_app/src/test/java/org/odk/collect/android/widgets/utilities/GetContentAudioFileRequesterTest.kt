package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.odk.collect.android.R
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.androidshared.system.IntentLauncherImpl
import org.odk.collect.testshared.ErrorIntentLauncher
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class GetContentAudioFileRequesterTest {
    private lateinit var intentLauncher: IntentLauncher
    private val waitingForDataRegistry = FakeWaitingForDataRegistry()
    private lateinit var activity: Activity
    private lateinit var requester: GetContentAudioFileRequester

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).get()
    }

    private fun setupRequester() {
        requester = GetContentAudioFileRequester(
            activity, intentLauncher, waitingForDataRegistry,
            mock(
                FormEntryViewModel::class.java
            )
        )
    }

    @Test
    fun requestFile_whenIntentIsNotAvailable_doesNotStartAnyIntentAndCancelsWaitingForData() {
        intentLauncher = ErrorIntentLauncher()
        setupRequester()
        requester.requestFile(QuestionWidgetHelpers.promptWithAnswer(null))
        val startedActivity = Shadows.shadowOf(activity).nextStartedActivity
        val toastMessage = ShadowToast.getTextOfLatestToast()
        assertThat(startedActivity, nullValue())
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true))
        assertThat(
            toastMessage,
            equalTo(
                activity.getString(
                    R.string.activity_not_found,
                    activity.getString(R.string.choose_sound)
                )
            )
        )
    }

    @Test
    fun requestFile_startsChooseAudioFileActivityAndSetsWidgetWaitingForData() {
        intentLauncher = IntentLauncherImpl
        setupRequester()
        val prompt = QuestionWidgetHelpers.promptWithAnswer(null)
        requester.requestFile(prompt)
        val startedActivity = Shadows.shadowOf(activity).nextStartedActivity
        assertThat(
            startedActivity.action,
            equalTo(Intent.ACTION_GET_CONTENT)
        )
        assertThat(startedActivity.type, equalTo("audio/*"))
        val intentForResult = Shadows.shadowOf(activity).nextStartedActivityForResult
        assertThat(
            intentForResult.requestCode,
            equalTo(ApplicationConstants.RequestCodes.AUDIO_CHOOSER)
        )
        assertThat(
            waitingForDataRegistry.waiting.contains(prompt.index),
            equalTo(true)
        )
    }
}
