package org.odk.collect.android.map;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

public class MapboxUtils {
    private static boolean initAttempted;
    private static Mapbox mapbox;

    private MapboxUtils() {

    }

    /** Attempts to initialize Mapbox; returns the singleton Mapbox if successful. */
    public static Mapbox initMapbox() {
        if (initAttempted) {
            return mapbox;
        }

        // To use the Mapbox base maps, we have to initialize the Mapbox SDK with
        // an access token. Configure this token in collect_app/secrets.properties.
        try {
            mapbox = Mapbox.getInstance(Collect.getInstance(), BuildConfig.MAPBOX_ACCESS_TOKEN);
        } catch (ExceptionInInitializerError e) {
            // Initialization failed (usually because the Mapbox native library for
            // the current architecture could not be found or loaded).
            mapbox = null;
        }

        // It's not safe to call Mapbox.getInstance() more than once.  It can fail on
        // the first call and then succeed on the second call, returning an invalid,
        // crashy Mapbox object.  We trust only the result of the first attempt.
        initAttempted = true;
        return mapbox;
    }

    /** Shows a warning that Mapbox is not supported by this device. */
    public static void warnMapboxUnsupported(Context context) {
        Toast.makeText(
            context,
            context.getString(R.string.mapbox_unsupported_warning, Build.CPU_ABI),
            Toast.LENGTH_LONG
        ).show();
    }
}
