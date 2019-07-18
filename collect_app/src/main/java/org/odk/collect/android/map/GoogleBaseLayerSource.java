package org.odk.collect.android.map;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.android.R;
import org.odk.collect.android.map.MbtilesFile.LayerType;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;

public class GoogleBaseLayerSource implements BaseLayerSource {
    // We must hold on to the listener or it will be garbage-collected.
    // See SharedPreferences.registerOnSharedPreferenceChangeListener().
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

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

    @Override public void addPrefs(PreferenceCategory category) {
        Context context = category.getContext();
        category.addPreference(
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

    @Override public MapFragment createMapFragment(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        GoogleMapFragment map = new GoogleMapFragment();
        setMapTypeFromPrefs(prefs, map);
        setReferenceLayerFromPrefs(prefs, map);

        listener = (SharedPreferences p, String key) -> {
            if (key.equals(KEY_GOOGLE_MAP_STYLE)) {
                setMapTypeFromPrefs(prefs, map);
            }
            if (key.equals(KEY_REFERENCE_LAYER)) {
                setReferenceLayerFromPrefs(prefs, map);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        return map;
    }

    private void setMapTypeFromPrefs(SharedPreferences prefs, GoogleMapFragment map) {
        map.setMapType(Integer.parseInt(prefs.getString(
            KEY_GOOGLE_MAP_STYLE, Integer.toString(GoogleMap.MAP_TYPE_NORMAL))));
    }

    private void setReferenceLayerFromPrefs(SharedPreferences prefs, GoogleMapFragment map) {
        String path = prefs.getString(KEY_REFERENCE_LAYER, null);
        map.setReferenceLayerFile(path != null ? new File(path) : null);
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
