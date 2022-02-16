package org.odk.collect.android.fragments;

import static org.odk.collect.settings.keys.MetaKeys.KEY_MAPBOX_INITIALIZED;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.shared.settings.Settings;

import javax.inject.Inject;

public class MapBoxInitializationFragment extends Fragment {

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    NetworkStateProvider connectivityProvider;

    private MapView mapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(getActivity()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mapbox_fragment_layout, container, false);
        initMapBox(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    private void initMapBox(View rootView) {
        Settings metaSharedPreferences = settingsProvider.getMetaSettings();
        if (!metaSharedPreferences.getBoolean(KEY_MAPBOX_INITIALIZED) && connectivityProvider.isDeviceOnline()) {
            // This "one weird trick" lets us initialize MapBox at app start when the internet is
            // most likely to be available. This is annoyingly needed for offline tiles to work.
            try {
                mapView = new MapView(getContext());
                FrameLayout mapBoxContainer = rootView.findViewById(R.id.map_box_container);
                mapBoxContainer.addView(mapView);
                mapView.getMapAsync(mapBoxMap -> mapBoxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                    metaSharedPreferences.save(KEY_MAPBOX_INITIALIZED, true);
                }));
            } catch (Exception | Error ignored) {
                // This will crash on devices where the arch for MapBox is not included
            }
        }
    }
}
