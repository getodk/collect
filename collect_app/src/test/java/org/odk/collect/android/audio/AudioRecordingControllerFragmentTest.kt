package org.odk.collect.android.audio

import android.app.Application
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.formentry.BackgroundAudioViewModel
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.formentry.FormSessionRepository
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ExternalWebPageHelper
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class AudioRecordingControllerFragmentTest {
    private lateinit var audioRecorder: StubAudioRecorder

    private val backgroundAudioViewModel = mock<BackgroundAudioViewModel>()
    private val formEntryViewModel = mock<FormEntryViewModel>()
    private val hasBackgroundRecording = MutableNonNullLiveData(false)
    private val isBackgroundRecordingEnabled = MutableNonNullLiveData(false)
    private val externalWebPageHelper = mock<ExternalWebPageHelper>()

    @get:Rule
    var launcherRule =
        FragmentScenarioLauncherRule(
            R.style.Theme_MaterialComponents,
            FragmentFactoryBuilder()
                .forClass(AudioRecordingControllerFragment::class.java) {
                    AudioRecordingControllerFragment(
                        "blah"
                    )
                }
                .build()
        )

    @Before
    fun setup() {
        val stubRecording = File.createTempFile("test", ".m4a")
        stubRecording.deleteOnExit()
        audioRecorder = StubAudioRecorder(stubRecording.absolutePath)
        whenever(formEntryViewModel.hasBackgroundRecording()).thenReturn(hasBackgroundRecording)
        whenever(backgroundAudioViewModel.isBackgroundRecordingEnabled).thenReturn(
            isBackgroundRecordingEnabled
        )

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesBackgroundAudioViewModelFactory(
                audioRecorder: AudioRecorder,
                settingsProvider: SettingsProvider,
                permissionsChecker: PermissionsChecker,
                analytics: Analytics,
                formSessionRepository: FormSessionRepository
            ): BackgroundAudioViewModel.Factory {
                return object : BackgroundAudioViewModel.Factory(
                    audioRecorder,
                    settingsProvider.getUnprotectedSettings(),
                    permissionsChecker,
                    Supplier { System.currentTimeMillis() },
                    formSessionRepository
                ) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return backgroundAudioViewModel as T
                    }
                }
            }

            override fun providesFormEntryViewModelFactory(
                scheduler: Scheduler,
                formSessionRepository: FormSessionRepository
            ): FormEntryViewModel.Factory {
                return object : FormEntryViewModel.Factory(
                    Supplier { System.currentTimeMillis() },
                    scheduler,
                    formSessionRepository
                ) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return formEntryViewModel as T
                    }
                }
            }

            override fun providesAudioRecorder(application: Application): AudioRecorder {
                return audioRecorder
            }

            override fun providesExternalWebPageHelper(): ExternalWebPageHelper {
                return externalWebPageHelper
            }
        })
    }

    @Test
    fun `updates timeCode`() {
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.timeCode.text.toString(),
                equalTo("00:00")
            )
            audioRecorder.duration = 40000
            assertThat(
                fragment.binding.timeCode.text.toString(),
                equalTo("00:40")
            )
        }
    }

    @Test
    fun `updates volumeBar`() {
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.volumeBar.latestAmplitude,
                equalTo(0)
            )
            audioRecorder.amplitude = 156
            assertThat(
                fragment.binding.volumeBar.latestAmplitude,
                equalTo(156)
            )
        }
    }

    @Test
    fun `clicking pause pauses recording`() {
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            fragment.binding.pauseRecording.performClick()
            assertThat(
                audioRecorder.getCurrentSession().value!!.paused,
                `is`(true)
            )
        }
    }

    @Test
    fun `when recording paused clicking pause resumes recording`() {
        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            fragment.binding.pauseRecording.performClick()
            assertThat(
                audioRecorder.getCurrentSession().value!!.paused,
                `is`(false)
            )
        }
    }

    @Test
    fun `when recording paused pause icon changes to resume`() {
        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                Shadows.shadowOf(fragment.binding.pauseRecording.icon).createdFromResId,
                `is`(R.drawable.ic_baseline_mic_24)
            )
            assertThat(
                fragment.binding.pauseRecording.contentDescription,
                `is`(fragment.getString(R.string.resume_recording))
            )
        }
    }

    @Test
    fun `when recording paused recording status changes to paused`() {
        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                Shadows.shadowOf(fragment.binding.recordingIcon.drawable).createdFromResId,
                `is`(R.drawable.ic_pause_24dp)
            )
        }
    }

    @Test
    fun `when recording resumed pause icon changes to pause`() {
        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()
        audioRecorder.resume()

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                Shadows.shadowOf(fragment.binding.pauseRecording.icon).createdFromResId,
                `is`(R.drawable.ic_pause_24dp)
            )
            assertThat(
                fragment.binding.pauseRecording.contentDescription,
                `is`(fragment.getString(R.string.pause_recording))
            )
        }
    }

    @Test
    fun `when recording resumed recording status changes to recording`() {
        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()
        audioRecorder.resume()

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                Shadows.shadowOf(fragment.binding.recordingIcon.drawable).createdFromResId,
                `is`(R.drawable.ic_baseline_mic_24)
            )
        }
    }

    @Test
    @Config(sdk = [23])
    fun `when SDK older than 24 hides pause button`() {
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.pauseRecording.visibility,
                `is`(
                    View.GONE
                )
            )
        }
    }

    @Test
    @Config(sdk = [24])
    fun `when SDK 24 or newer shows pause button`() {
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.pauseRecording.visibility,
                `is`(
                    View.VISIBLE
                )
            )
        }
    }

    @Test
    fun `when form has background recording hides controls`() {
        hasBackgroundRecording.value = true
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.controls.visibility,
                `is`(
                    View.GONE
                )
            )
        }
    }

    @Test
    fun `when form has background recording clicking help button opens help dialog`() {
        hasBackgroundRecording.value = true
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.help.visibility,
                `is`(
                    View.VISIBLE
                )
            )
            fragment.binding.help.performClick()
            val dialog = getFragmentByClass(
                fragment.parentFragmentManager,
                BackgroundAudioHelpDialogFragment::class.java
            )
            assertThat(dialog, notNullValue())
        }
    }

    @Test
    fun `when form does not have background recording hides help button`() {
        hasBackgroundRecording.value = false
        audioRecorder.start("session", Output.AAC)

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.help.visibility,
                `is`(
                    View.GONE
                )
            )
        }
    }

    @Test
    fun `when there is an error starting recording shows error dialog`() {
        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        audioRecorder.failOnStart()
        audioRecorder.start("blah", Output.AAC)

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            val dialog = getFragmentByClass(
                fragment.parentFragmentManager,
                AudioRecordingErrorDialogFragment::class.java
            )
            assertThat(dialog, notNullValue())
        }
    }

    @Test
    fun `when form has background recording and background recording is disabled shows that recording is disabled`() {
        hasBackgroundRecording.value = true
        isBackgroundRecordingEnabled.value = false

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.root.visibility,
                `is`(
                    View.VISIBLE
                )
            )
            assertThat(
                fragment.binding.timeCode.text,
                `is`(fragment.getString(R.string.recording_disabled, "⋮"))
            )
            assertThat(
                fragment.binding.volumeBar.visibility,
                `is`(View.GONE)
            )
            assertThat(fragment.binding.controls.visibility, `is`(View.GONE))
            assertThat(fragment.binding.help.visibility, `is`(View.GONE))
        }
    }

    @Test
    fun `when form does not have background recording and background recording is disabled does not show recording is disabled`() {
        hasBackgroundRecording.value = false
        isBackgroundRecordingEnabled.value = false
        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )
        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.root.visibility,
                `is`(
                    View.GONE
                )
            )
        }
    }

    @Test
    fun `when form has background recording and there is an error and session is over shows that there is an error`() {
        hasBackgroundRecording.value = true
        isBackgroundRecordingEnabled.value = true

        audioRecorder.failOnStart()

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        audioRecorder.start("blah", Output.AAC_LOW)
        audioRecorder.cleanUp()

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.root.visibility,
                `is`(
                    View.VISIBLE
                )
            )
            assertThat(
                fragment.binding.timeCode.text,
                `is`(fragment.getString(R.string.start_recording_failed))
            )
            assertThat(
                fragment.binding.volumeBar.visibility,
                `is`(View.GONE)
            )
            assertThat(fragment.binding.controls.visibility, `is`(View.GONE))
            assertThat(fragment.binding.help.visibility, `is`(View.GONE))
        }
    }

    @Test
    fun `when form does not have background recording and there is an error and session is over does not that there is an error`() {
        hasBackgroundRecording.value = false
        isBackgroundRecordingEnabled.value = true
        audioRecorder.failOnStart()

        val scenario = launcherRule.launchInContainer(
            AudioRecordingControllerFragment::class.java
        )

        audioRecorder.start("blah", Output.AAC_LOW)
        audioRecorder.cleanUp()

        scenario.onFragment { fragment: AudioRecordingControllerFragment ->
            assertThat(
                fragment.binding.root.visibility,
                `is`(
                    View.GONE
                )
            )
        }
    }
}
