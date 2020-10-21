package org.odk.collect.audiorecorder.recording

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class AudioRecorderViewModelTest {

    @Test
    fun onCleared_startsRecorderService_withCancel() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AudioRecorderViewModel(application, RecordingRepository())

        viewModel.onCleared()
        val nextStartedService = shadowOf(application).nextStartedService
        assertThat(nextStartedService.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
        assertThat(nextStartedService.action, equalTo(AudioRecorderService.ACTION_CANCEL))
    }
}
