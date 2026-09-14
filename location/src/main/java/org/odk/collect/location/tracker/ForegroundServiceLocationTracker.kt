package org.odk.collect.location.tracker

import android.app.ActivityManager
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.odk.collect.androidshared.data.getState
import org.odk.collect.androidshared.ui.ReturnToAppActivity
import org.odk.collect.androidshared.utils.UniqueIdGenerator
import org.odk.collect.location.Location
import org.odk.collect.location.LocationClient
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.location.LocationDependencyComponentProvider
import org.odk.collect.strings.localization.getLocalizedString
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val LOCATION_KEY = "location"

class ForegroundServiceLocationTracker(private val application: Application) : LocationTracker {

    override fun getLocation(): StateFlow<Location?> {
        return application.getState().getFlow(LOCATION_KEY, null)
    }

    override fun start(retainMockAccuracy: Boolean, updateInterval: Long?, notification: Boolean) {
        val intent = Intent(application, LocationTrackerService::class.java).also { intent ->
            intent.putExtra(LocationTrackerService.EXTRA_RETAIN_MOCK_ACCURACY, retainMockAccuracy)
            intent.putExtra(LocationTrackerService.EXTRA_NOTIFICATION, notification)
            updateInterval?.let {
                intent.putExtra(LocationTrackerService.EXTRA_UPDATE_INTERVAL, it)
            }
        }

        if (notification) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }
    }

    override fun stop() {
        application.stopService(Intent(application, LocationTrackerService::class.java))
    }

    override fun bindToLifecycle(
        context: Context,
        lifecycle: LifecycleOwner,
        retainMockAccuracy: Boolean
    ) {
        lifecycle.lifecycle.addObserver(object : DefaultLifecycleObserver {
            var delayedCheckScope = CoroutineScope(Dispatchers.Main)

            override fun onResume(owner: LifecycleOwner) {
                /**
                 * Avoid starting service in background (even after `onResume`) due to Android
                 * issue: https://issuetracker.google.com/u/2/issues/110237673.
                 */
                delayedCheckScope.launch {
                    while (!isAppInForeground(context)) {
                        delay(100.milliseconds)
                    }

                    start(
                        retainMockAccuracy = retainMockAccuracy,
                        updateInterval = null,
                        notification = false
                    )
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                delayedCheckScope.cancel()
                delayedCheckScope = CoroutineScope(Dispatchers.Main)

                stop()
            }
        })
    }
}

class LocationTrackerService : Service(), LocationClient.LocationClientListener {

    @Inject
    lateinit var uniqueIdGenerator: UniqueIdGenerator

    private val locationClient: LocationClient by lazy {
        LocationClientProvider.getClient(application)
    }

    override fun onCreate() {
        super.onCreate()
        val provider = applicationContext as LocationDependencyComponentProvider
        provider.locationDependencyComponent.inject(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_NOTIFICATION, true) ?: true) {
            setupNotificationChannel()
            startForeground(
                uniqueIdGenerator.getInt(NOTIFICATION_IDENTIFIER),
                createNotification()
            )
        }

        locationClient.setRetainMockAccuracy(
            intent?.getBooleanExtra(
                EXTRA_RETAIN_MOCK_ACCURACY,
                false
            ) ?: false
        )

        if (intent?.hasExtra(EXTRA_UPDATE_INTERVAL) == true) {
            val interval = intent.getLongExtra(EXTRA_UPDATE_INTERVAL, -1)
            locationClient.setUpdateInterval(interval)
        }

        locationClient.start(this)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        locationClient.stop()
        application.getState().setFlow(LOCATION_KEY, null)
    }

    override fun onClientStart() {
        locationClient.requestLocationUpdates {
            application.getState().setFlow(
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
        const val EXTRA_NOTIFICATION = "notification"

        private const val NOTIFICATION_IDENTIFIER = "location_tracking"
        private const val NOTIFICATION_CHANNEL = "location_tracking"
    }
}

private fun isAppInForeground(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses ?: return false

    val packageName = context.packageName
    for (appProcess in appProcesses) {
        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
            appProcess.processName == packageName) {
            return true
        }
    }

    return false
}
