package odk.hedera.collect.utilities;

import android.location.Location;
import android.os.Bundle;

import odk.hedera.collect.storage.StoragePathProvider;

import java.io.File;

import static odk.hedera.collect.geo.MapFragment.KEY_REFERENCE_LAYER;

public class GeoUtils {

    private GeoUtils() {

    }

    public static String formatLocationResultString(Location location) {
        return String.format("%s %s %s %s", location.getLatitude(), location.getLongitude(),
                location.getAltitude(), location.getAccuracy());
    }

    /**
     * Corrects location provider names so "gps" displays as "GPS" in user-facing messaging.
     */
    public static String capitalizeGps(String locationProvider) {
        return "gps".equals(locationProvider) ? "GPS" : locationProvider;
    }

    public static File getReferenceLayerFile(Bundle config, StoragePathProvider storagePathProvider) {
        String path = storagePathProvider.getAbsoluteOfflineMapLayerPath(config.getString(KEY_REFERENCE_LAYER));
        return path != null && new File(path).exists()
                ? new File(path)
                : null;
    }
}
