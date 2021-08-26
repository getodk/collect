package org.odk.collect.android.geo;

import com.mapbox.mapboxsdk.Mapbox;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;

public final class MapboxUtils {
    private static boolean initAttempted;
    private static Mapbox mapbox;

    private MapboxUtils() {

    }

    /** Attempts to initialize Mapbox; returns the singleton Mapbox if successful. */
    public static Mapbox initMapbox() {
        if (initAttempted) {
            return mapbox;
        }

        try {
            // To use the Mapbox base maps, we have to initialize the Mapbox SDK with
            // an access token. Configure this token in collect_app/secrets.properties.
            mapbox = Mapbox.getInstance(Collect.getInstance(), BuildConfig.MAPBOX_ACCESS_TOKEN);
            Mapbox.setConnected(true);
        } catch (Exception | Error e) {
            // To keep our APK from getting too big, we decided to include the
            // Mapbox native library only for the most common binary architectures.
            // So, on a small minority of Android devices, initialization of the
            // Mapbox SDK will fail.
            mapbox = null;
        }

        // It's not safe to call Mapbox.getInstance() more than once.  It can fail on
        // the first call and then succeed on the second call, returning an invalid,
        // crashy Mapbox object.  We trust only the result of the first attempt.
        initAttempted = true;
        return mapbox;
    }
}
