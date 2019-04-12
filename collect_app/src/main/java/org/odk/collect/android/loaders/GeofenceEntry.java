package org.odk.collect.android.loaders;

import android.location.Location;

/**
 * This class holds details on a geofence
 */
public class GeofenceEntry {
    public int showDist;
    public Location location;
    public boolean in = false;      // Set true if the user is currently inside the geofence

    public GeofenceEntry(int showDist, Location location) {
        this.showDist = showDist;
        this.location = location;
    }
}

