package org.odk.collect.location

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.core.location.LocationManagerCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object LocationUtils {

    @JvmStatic
    @JvmOverloads
    fun sanitizeAccuracy(location: Location?, retainMockAccuracy: Boolean = false): Location? {
        if (location != null && (location.isFromMockProvider && !retainMockAccuracy || location.accuracy < 0)) {
            location.accuracy = 0f
        }

        return location
    }

    @JvmStatic
    fun checkLocationServicesEnabled(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (LocationManagerCompat.isLocationEnabled(locationManager)) {
            true
        } else {
            MaterialAlertDialogBuilder(activity)
                .setMessage(activity.getString(R.string.gps_enable_message))
                .setCancelable(false)
                .setPositiveButton(
                    activity.getString(R.string.enable_gps),
                    DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->
                        activity.startActivityForResult(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0
                        )
                    }
                )
                .setNegativeButton(
                    activity.getString(R.string.cancel),
                    DialogInterface.OnClickListener { dialog: DialogInterface, id: Int -> dialog.cancel() }
                )
                .create()
                .show()

            false
        }
    }
}
