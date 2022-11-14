package org.odk.collect.android.audio

import android.app.Application
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.testshared.RobolectricHelpers.runLooper
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioRecordingFormErrorDialogFragmentTest {
    private lateinit var audioRecorder: StubAudioRecorder

    @get:Rule
    var launcherRule = FragmentScenarioLauncherRule(
        R.style.Theme_MaterialComponents
    )

    @Before
    fun setup() {
        val stubRecording = File.createTempFile("test", ".m4a")
        stubRecording.deleteOnExit()
        audioRecorder = StubAudioRecorder(stubRecording.absolutePath)
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesAudioRecorder(application: Application): AudioRecorder {
                return audioRecorder
            }
        })
    }

    @Test
    fun `clicking OK dismisses dialog`() {
        val scenario = launcherRule.launch(
            AudioRecordingErrorDialogFragment::class.java
        )
        scenario.onFragment { f: AudioRecordingErrorDialogFragment ->
            val dialog = f.dialog as AlertDialog?
            val button = dialog!!.getButton(DialogInterface.BUTTON_POSITIVE)

            assertThat(button.text, `is`(f.getString(R.string.ok)))

            button.performClick()
            runLooper()

            assertFalse(dialog.isShowing)
        }
    }

    @Test
    fun `dismiss consumes consumable`() {
        val scenario = launcherRule.launch(
            AudioRecordingErrorDialogFragment::class.java
        )
        scenario.onFragment { obj: DialogFragment -> obj.dismiss() }

        assertTrue(audioRecorder.failedToStart().value!!.isConsumed())
    }
}
