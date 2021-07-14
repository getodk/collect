package org.odk.collect.location.tracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.GoogleApiAvailability
import org.odk.collect.location.GoogleFusedLocationClient
import org.odk.collect.location.Location
import org.odk.collect.location.LocationClient
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.location.R

private var location: Location? = null

class ForegroundServiceLocationTracker(private val context: Context) : LocationTracker {

    init {
        location = null // Clear static location for new instance
    }

    override fun getCurrentLocation(): Location? {
        return location
    }

    override fun start() {
        context.startService(Intent(context, LocationTrackerService::class.java))
    }

    override fun stop() {
        context.stopService(Intent(context, LocationTrackerService::class.java))
    }
}

class LocationTrackerService : Service() {

    private val locationClient: LocationClient by lazy {
        LocationClientProvider.getClient(
            this,
            { GoogleFusedLocationClient(application) },
            GoogleApiAvailability.getInstance()
        )
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

        locationClient.setListener(object : LocationClient.LocationClientListener {
            override fun onClientStart() {
                locationClient.requestLocationUpdates {
                    location = Location(it.latitude, it.longitude, it.altitude, it.accuracy)
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
    }

    private fun createNotification() = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
        .setContentTitle("Tracking location...")
        .setSmallIcon(R.drawable.ic_baseline_location_searching_24)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                "Location tracking",
                NotificationManager.IMPORTANCE_LOW
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "location_tracking"
    }
}
