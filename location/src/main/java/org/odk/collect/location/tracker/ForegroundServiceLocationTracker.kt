package org.odk.collect.location.tracker

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.androidshared.data.getState
import org.odk.collect.androidshared.ui.ReturnToAppActivity
import org.odk.collect.androidshared.utils.UniqueIdGenerator
import org.odk.collect.location.Location
import org.odk.collect.location.LocationClient
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.location.LocationDependencyComponentProvider
import org.odk.collect.strings.localization.getLocalizedString
import javax.inject.Inject

class ForegroundServiceLocationTracker(private val application: Application) : LocationTracker {

    private val locationClientHandler = LocationClientHandler(application, LOCATION_KEY)

    override fun getLocation(): StateFlow<Location?> {
        return application.getState().getFlow(LOCATION_KEY, null)
    }

    override fun start(retainMockAccuracy: Boolean, updateInterval: Long?, notification: Boolean) {
        if (notification) {
            val intent = Intent(application, LocationTrackerService::class.java).also { intent ->
                intent.putExtra(LocationTrackerService.EXTRA_RETAIN_MOCK_ACCURACY, retainMockAccuracy)
                updateInterval?.let {
                    intent.putExtra(LocationTrackerService.EXTRA_UPDATE_INTERVAL, it)
                }
                intent.putExtra(LocationTrackerService.EXTRA_STATE_KEY, LOCATION_KEY)
            }

            application.startForegroundService(intent)
        } else {
            locationClientHandler.start(retainMockAccuracy, updateInterval)
        }
    }

    override fun stop() {
        application.stopService(Intent(application, LocationTrackerService::class.java))
        locationClientHandler.stop()
    }

    override fun bindToLifecycle(
        lifecycle: LifecycleOwner,
        retainMockAccuracy: Boolean
    ) {
        lifecycle.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                start(
                    retainMockAccuracy = retainMockAccuracy,
                    updateInterval = null,
                    notification = false
                )
            }

            override fun onPause(owner: LifecycleOwner) {
                stop()
            }
        })
    }

    companion object {
        private const val LOCATION_KEY = "location"
    }
}

class LocationTrackerService : Service() {

    @Inject
    lateinit var uniqueIdGenerator: UniqueIdGenerator

    private var locationClientHandler: LocationClientHandler? = null

    override fun onCreate() {
        super.onCreate()
        val provider = applicationContext as LocationDependencyComponentProvider
        provider.locationDependencyComponent.inject(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotificationChannel()
        startForeground(
            uniqueIdGenerator.getInt(NOTIFICATION_IDENTIFIER),
            createNotification()
        )

        val retainMockAccuracy = intent?.getBooleanExtra(
            EXTRA_RETAIN_MOCK_ACCURACY,
            false
        ) ?: false

        val updateInterval = if (intent?.hasExtra(EXTRA_UPDATE_INTERVAL) == true) {
            intent.getLongExtra(EXTRA_UPDATE_INTERVAL, -1)
        } else {
            null
        }

        val stateKey = intent?.getStringExtra(EXTRA_STATE_KEY)
        if (stateKey != null) {
            locationClientHandler = LocationClientHandler(application, stateKey).also {
                it.start(retainMockAccuracy, updateInterval)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        locationClientHandler?.stop()
    }

    private fun createNotification(): Notification {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            .setContentTitle(getLocalizedString(org.odk.collect.strings.R.string.location_tracking_notification_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(createNotificationIntent())

        return notification
            .build()
    }

    private fun createNotificationIntent() =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, ReturnToAppActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun setupNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            getLocalizedString(org.odk.collect.strings.R.string.location_tracking_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            notificationChannel
        )
    }

    companion object {
        const val EXTRA_RETAIN_MOCK_ACCURACY = "retain_mock_accuracy"
        const val EXTRA_UPDATE_INTERVAL = "update_interval"
        const val EXTRA_STATE_KEY = "state_key"

        private const val NOTIFICATION_IDENTIFIER = "location_tracking"
        private const val NOTIFICATION_CHANNEL = "location_tracking"
    }
}

private class LocationClientHandler(private val application: Application, val stateKey: String) : LocationClient.LocationClientListener {

    private val locationClient: LocationClient by lazy {
        LocationClientProvider.getClient(application)
    }

    fun start(retainMockAccuracy: Boolean, updateInterval: Long?) {
        locationClient.setRetainMockAccuracy(retainMockAccuracy)

        updateInterval?.let {
            locationClient.setUpdateInterval(it)
        }

        locationClient.start(this)
    }

    fun stop() {
        locationClient.stop()
        application.getState().setFlow(stateKey, null)
    }

    override fun onClientStart() {
        locationClient.requestLocationUpdates {
            application.getState().setFlow(
                stateKey,
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
}
