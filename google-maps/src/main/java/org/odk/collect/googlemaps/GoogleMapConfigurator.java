package org.odk.collect.googlemaps;

import static org.odk.collect.androidshared.ui.PrefUtils.createListPref;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_GOOGLE_MAP_STYLE;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.preference.Preference;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.androidshared.system.OpenGLVersionChecker;
import org.odk.collect.androidshared.system.PlayServicesChecker;
import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.maps.MapConfigurator;
import org.odk.collect.maps.layers.MbtilesFile;
import org.odk.collect.maps.layers.MbtilesFile.LayerType;
import org.odk.collect.shared.settings.Settings;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class GoogleMapConfigurator implements MapConfigurator {
    private final int sourceLabelId = org.odk.collect.strings.R.string.basemap_source_google;
    private final GoogleMapTypeOption[] options = {
            new GoogleMapTypeOption(GoogleMap.MAP_TYPE_NORMAL, org.odk.collect.strings.R.string.streets),
            new GoogleMapTypeOption(GoogleMap.MAP_TYPE_TERRAIN, org.odk.collect.strings.R.string.terrain),
            new GoogleMapTypeOption(GoogleMap.MAP_TYPE_HYBRID, org.odk.collect.strings.R.string.hybrid),
            new GoogleMapTypeOption(GoogleMap.MAP_TYPE_SATELLITE, org.odk.collect.strings.R.string.satellite)
    };

    @Override public boolean isAvailable(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY");

            return isGoogleMapsSdkAvailable(context) && isGooglePlayServicesAvailable(context) && !apiKey.equals("");
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isGooglePlayServicesAvailable(Context context) {
        return new PlayServicesChecker().isGooglePlayServicesAvailable(context);
    }

    private static boolean isGoogleMapsSdkAvailable(Context context) {
        /*
         * The Google Maps SDK for Android requires OpenGL ES version 2.
         * See: https://developers.google.com/maps/documentation/android-sdk/config
         */
        return OpenGLVersionChecker.isOpenGLv2Supported(context);
    }

    @Override public void showUnavailableMessage(Context context) {
        if (!isGoogleMapsSdkAvailable(context)) {
            ToastUtils.showLongToast(context.getString(
                org.odk.collect.strings.R.string.basemap_source_unavailable, context.getString(sourceLabelId)));
        }

        PlayServicesChecker playServicesChecker = new PlayServicesChecker();
        if (!playServicesChecker.isGooglePlayServicesAvailable(context)) {
            playServicesChecker.showGooglePlayServicesAvailabilityErrorDialog(context);
        }
    }

    @Override public List<Preference> createPrefs(Context context, Settings settings) {
        int[] labelIds = new int[options.length];
        String[] values = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labelIds[i] = options[i].labelId;
            values[i] = Integer.toString(options[i].mapType);
        }
        String prefTitle = context.getString(
            org.odk.collect.strings.R.string.map_style_label, context.getString(sourceLabelId));
        return Collections.singletonList(createListPref(
            context, KEY_GOOGLE_MAP_STYLE, prefTitle, labelIds, values, settings
        ));
    }

    @Override public boolean supportsLayer(File file) {
        // GoogleMapFragment supports only raster tiles.
        return MbtilesFile.readLayerType(file) == LayerType.RASTER;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.readName(file);
        return name != null ? name : file.getName();
    }

    public static class GoogleMapTypeOption {
        final int mapType;
        final int labelId;

        public GoogleMapTypeOption(int mapType, int labelId) {
            this.mapType = mapType;
            this.labelId = labelId;
        }
    }
}
