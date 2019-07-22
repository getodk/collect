package org.odk.collect.android.geo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

import com.google.android.gms.maps.GoogleMap;
import com.google.common.collect.ImmutableSet;

import org.odk.collect.android.R;
import org.odk.collect.android.geo.MbtilesFile.LayerType;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

class GoogleMapConfigurator implements MapConfigurator {
    @Override public boolean isAvailable(Context context) {
        return isGoogleMapsSdkAvailable(context) && isGooglePlayServicesAvailable(context);
    }

    @Override public void showUnavailableMessage(Context context) {
        if (!isGoogleMapsSdkAvailable(context)) {
            ToastUtils.showLongToast(R.string.google_maps_sdk_unavailable_warning);
        }
        if (!isGooglePlayServicesAvailable(context)) {
            PlayServicesUtil.showGooglePlayServicesAvailabilityErrorDialog(context);
        }
    }

    private boolean isGoogleMapsSdkAvailable(Context context) {
        // The Google Maps SDK for Android requires OpenGL ES version 2.
        // See https://developers.google.com/maps/documentation/android-sdk/config
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo().reqGlEsVersion >= 0x20000;
    }

    private boolean isGooglePlayServicesAvailable(Context context) {
        return PlayServicesUtil.isGooglePlayServicesAvailable(context);
    }

    @Override public MapFragment createMapFragment(Context context) {
        return new GoogleMapFragment();
    }

    @Override public List<Preference> createPrefs(Context context) {
        return Collections.singletonList(
            PrefUtils.createListPref(
                context,
                KEY_GOOGLE_MAP_STYLE,
                R.string.google_map_style,
                new int[] {
                    R.string.google_map_style_streets,
                    R.string.google_map_style_terrain,
                    R.string.google_map_style_hybrid,
                    R.string.google_map_style_satellite,
                },
                new String[] {
                    Integer.toString(GoogleMap.MAP_TYPE_NORMAL),
                    Integer.toString(GoogleMap.MAP_TYPE_TERRAIN),
                    Integer.toString(GoogleMap.MAP_TYPE_HYBRID),
                    Integer.toString(GoogleMap.MAP_TYPE_SATELLITE)
                }
            )
        );
    }

    @Override public Collection<String> getPrefKeys() {
        return ImmutableSet.of(KEY_GOOGLE_MAP_STYLE, KEY_REFERENCE_LAYER);
    }

    @Override public Bundle buildConfig(SharedPreferences prefs) {
        int mapType;
        try {
            mapType = Integer.parseInt(prefs.getString(KEY_GOOGLE_MAP_STYLE, ""));
        } catch (NumberFormatException e) {
            mapType = GoogleMap.MAP_TYPE_NORMAL;
        }
        Bundle config = new Bundle();
        config.putInt(GoogleMapFragment.KEY_MAP_TYPE, mapType);
        config.putString(GoogleMapFragment.KEY_REFERENCE_LAYER,
            prefs.getString(KEY_REFERENCE_LAYER, null));
        return config;
    }

    @Override public boolean supportsLayer(File file) {
        // GoogleMapFragment supports only raster tiles.
        return MbtilesFile.getLayerType(file) == LayerType.RASTER;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.getName(file);
        return name != null ? name : file.getName();
    }
}
