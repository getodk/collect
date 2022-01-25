package org.odk.collect.location.tracker

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.odk.collect.androidshared.data.getState
import org.odk.collect.androidshared.ui.ReturnToAppActivity
import org.odk.collect.location.Location
import org.odk.collect.location.LocationClient
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.location.R
import org.odk.collect.strings.localization.getLocalizedString

private const val LOCATION_KEY = "location"

class ForegroundServiceLocationTracker(private val application: Application) : LocationTracker {

    override fun getCurrentLocation(): Location? {
        return application.getState().get(LOCATION_KEY)
    }

    override fun start(retainMockAccuracy: Boolean, updateInterval: Long?) {
        val intent = Intent(application, LocationTrackerService::class.java).also { intent ->
            intent.putExtra(LocationTrackerService.EXTRA_RETAIN_MOCK_ACCURACY, retainMockAccuracy)
            updateInterval?.let {
                intent.putExtra(LocationTrackerService.EXTRA_UPDATE_INTERVAL, it)
            }
        }

        application.startService(intent)
    }

    override fun stop() {
        application.stopService(Intent(application, LocationTrackerService::class.java))
    }
}

class LocationTrackerService : Service() {

    private val locationClient: LocationClient by lazy {
        LocationClientProvider.getClient(application)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        locationClient.setRetainMockAccuracy(
            intent?.getBooleanExtra(
                EXTRA_RETAIN_MOCK_ACCURACY,
                false
            ) ?: false
        )

        if (intent?.hasExtra(EXTRA_UPDATE_INTERVAL) == true) {
            val interval = intent.getLongExtra(EXTRA_UPDATE_INTERVAL, -1)
            locationClient.setUpdateIntervals(
                interval,
                interval / 2
            )
        }

        locationClient.setListener(object : LocationClient.LocationClientListener {
            override fun onClientStart() {
                locationClient.requestLocationUpdates {
                    application.getState().set(
                        LOCATION_KEY,
                        Location(it.latitude, it.longitude, it.altitude, it.accuracy)
                    )
                }
            }

            override fun onClientStartFailure() {
                // Ignored
            }

            override fun onClientStop() {
                // Ignored
            }
        })

        locationClient.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        locationClient.stop()
        application.getState().clear(LOCATION_KEY)
    }

    private fun createNotification(): Notification {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(getLocalizedString(R.string.location_tracking_notification_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(createNotificationIntent())

        return notification
            .build()
    }

    private fun createNotificationIntent() =
        PendingIntent.getActivity(this, 0, Intent(this, ReturnToAppActivity::class.java), 0)

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                getLocalizedString(R.string.location_tracking_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )
        }
    }

    companion object {
        const val EXTRA_RETAIN_MOCK_ACCURACY = "retain_mock_accuracy"
        const val EXTRA_UPDATE_INTERVAL = "update_interval"

        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "location_tracking"
    }
}
