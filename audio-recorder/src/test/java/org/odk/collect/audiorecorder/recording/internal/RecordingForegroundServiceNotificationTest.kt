package org.odk.collect.audiorecorder.recording.internal

import android.app.Application
import android.app.NotificationManager
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.data.getState
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class RecordingForegroundServiceNotificationTest {

    @Test
    fun dismiss_stopsUpdatingNotification() {
        val service = Robolectric.buildService(TestService::class.java).get()
        val recordingRepository = RecordingRepository(ApplicationProvider.getApplicationContext<Application>().getState())
        val recordingForegroundServiceNotification = RecordingForegroundServiceNotification(service, recordingRepository)

        recordingForegroundServiceNotification.show()
        recordingForegroundServiceNotification.dismiss()

        recordingRepository.start("session")
        recordingRepository.setDuration(5000)
        val notificationManager = shadowOf(service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        assertThat(shadowOf(notificationManager.allNotifications[0]).contentText, equalTo("00:00"))
    }
}

private class TestService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
