package org.odk.collect.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationResult;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TraceUtilities;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.loaders.GeofenceEntry;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.smap.utilities.LocationRegister;
import org.odk.collect.android.utilities.Constants;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

import static java.lang.StrictMath.abs;

/*
 * Accept broadcast locations from a pending intent
 */

public class LocationReceiver  extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATES =
            "au.com.smap.location_service.action.PROCESS_UPDATES";

    @Inject
    Notifier notifier;

    private void deferDaggerInit(Context context) {
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    for (Location location : result.getLocations()) {
                        if (location != null) {
                            onLocationChanged(context, location);
                        }
                    }
                }
            }
        }
    }

    public void onLocationChanged(Context context, Location location) {

        deferDaggerInit(context);

        if(isValidLocation(location) && isAccurateLocation(location)) {

            Collect.getInstance().setLocation(location);
            Location lastLocation = Collect.getInstance().getSavedLocation();

            if(lastLocation == null || location.distanceTo(lastLocation) > Constants.GPS_DISTANCE ) {

                Collect.getInstance().setSavedLocation(location);

                /*
                 * Test for geofence change if the user has moved more than the minimum distance
                 */
                ArrayList<GeofenceEntry> geofences = Collect.getInstance().getGeofences();
                if (geofences.size() > 0) {
                    boolean refresh = false;
                    boolean notify = false;
                    for (GeofenceEntry gfe : geofences) {
                        double yDistance = abs(location.getLatitude() - gfe.location.getLatitude()) * 111111.1;     // lattitude difference in meters
                        if (gfe.in) {                                                        // Currently inside
                            if (location.distanceTo(gfe.location) > gfe.showDist) {         // detailed check only
                                refresh = true;
                            }
                        } else {
                            if (yDistance < gfe.showDist) {                                      // Currently outside do rough check first
                                if (location.distanceTo(gfe.location) < gfe.showDist) {
                                    refresh = true;
                                    notify = true;
                                    break;      // No need to check more we have a notify and a refresh
                                }
                            }
                        }
                    }
                    if (refresh) {
                        Intent intent = new Intent("org.smap.smapTask.refresh");
                        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
                        Timber.i("######## send org.smap.smapTask.refresh from location service");  // smap
                    }
                    if (notify) {
                        notifier.showNotification(null,
                                NotificationActivity.NOTIFICATION_ID,
                                R.string.app_name,
                                context.getString(R.string.smap_geofence_tasks), false);
                    }
                }

                /*
                 * Process the location change
                 * Discard if this app is downloaded from the google play store
                 * Otherwise for corporate apps the response will be dependent on the corporate policy
                 */
                LocationRegister lr = new LocationRegister();
                lr.register(context, location);
            }

        }
    }

    /*
     * Check to see if this is a valid location
     */
    private boolean isAccurateLocation(Location location) {

        boolean accurate = true;
        if (!location.hasAccuracy() || location.getAccuracy() >= Constants.GPS_ACCURACY) {
            Timber.i("===== Inaccurate location");
            accurate = false;
        }
        return accurate;
    }


    /*
     * Check to see if this is a valid location
     */
    private boolean isValidLocation(Location location) {
        boolean valid = true;
        if(location == null || Math.abs(location.getLatitude()) > 90
                || Math.abs(location.getLongitude()) > 180) {
            valid = false;
        }

        // Return false if the location is 0 0, more likely than not this is a bad location
        if(Math.abs(location.getLongitude()) == 0.0 && Math.abs(location.getLongitude()) == 0.0) {
            Timber.i("===== Invalid location");

            valid = false;
        }

        return valid;
    }
}
