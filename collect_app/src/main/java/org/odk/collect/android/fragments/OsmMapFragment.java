package org.odk.collect.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.odk.collect.android.R;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

/** A Fragment that just contains a single org.osmdroid.views.MapView. */
public class OsmMapFragment extends Fragment {
    private MapView mapView;
    private final List<MapReadyCallback> mapReadyCallbacks = new ArrayList<>();

    /** An interface for receiving the MapView object when it is ready. */
    public interface MapReadyCallback {
        void onMapReady(MapView map);
    }

    /**
     * Ensures that the callback will be invoked once with the MapView object,
     * either now (if the MapView already exists) or later (when it is created).
     */
    public void getMapAsync(MapReadyCallback callback) {
        mapReadyCallbacks.add(callback);
        invokeMapReadyCallbacks();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.osm_map_layout, container, false);
        mapView = view.findViewById(R.id.osm_map_view);
        invokeMapReadyCallbacks();
        return view;
    }

    private void invokeMapReadyCallbacks() {
        while (mapView != null && !mapReadyCallbacks.isEmpty()) {
            mapReadyCallbacks.remove(0).onMapReady(mapView);
        }
    }
}
