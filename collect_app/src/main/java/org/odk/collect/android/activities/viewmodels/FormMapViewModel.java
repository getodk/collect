package org.odk.collect.android.activities.viewmodels;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.geo.MapFragment;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import timber.log.Timber;

public class FormMapViewModel extends ViewModel {
    /**
     * The form that is mapped.
     */
    private final Form form;

    /**
     * Cached count of all of this form's instances, including ones that don't have a geometry to
     * map.
     */
    private int totalInstanceCount;

    /**
     * Quick lookup of instance objects from map feature IDs.
     */
    private final Map<Integer, Instance> instancesByFeatureId = new HashMap<>();

    /**
     * Points to be mapped. Note: kept separately from {@link #instancesByFeatureId} so we can
     * quickly zoom to bounding box.
     */
    private final List<MapPoint> points = new ArrayList<>();

    /**
     * True if the map viewport has been initialized, false otherwise.
     */
    private boolean viewportInitialized;

    private final InstancesRepository instancesRepository;

    public FormMapViewModel(Form form, InstancesRepository instancesRepository) {
        this.instancesRepository = instancesRepository;
        this.form = form;
    }

    public String getFormTitle() {
        return form.getDisplayName();
    }

    public String getFormId() {
        return form.getJrFormId();
    }

    public int getInstanceCount() {
        return totalInstanceCount;
    }

    public int getMappedPointCount() {
        return points.size();
    }

    /**
     * Clears the existing features on the given map and places features for the current form's
     * instances. If features were added to the map and the map view was not previously initialized,
     * zooms to display all points.
     */
    public void mapUpdateRequested(MapFragment map) {
        points.clear();
        map.clearFeatures();

        List<Instance> instances = instancesRepository.getAllBy(form.getJrFormId());
        totalInstanceCount = instances.size();
        for (Instance instance : instances) {
            if (instance.getGeometry() != null) {
                try {
                    JSONObject geometry = new JSONObject(instance.getGeometry());
                    switch (instance.getGeometryType()) {
                        case "Point":
                            JSONArray coordinates = geometry.getJSONArray("coordinates");
                            // In GeoJSON, longitude comes before latitude.
                            double lon = coordinates.getDouble(0);
                            double lat = coordinates.getDouble(1);

                            MapPoint point = new MapPoint(lat, lon);
                            int featureId = map.addMarker(point, false);

                            int drawableId = getDrawableIdForStatus(instance.getStatus());
                            map.setMarkerIcon(featureId, drawableId);

                            instancesByFeatureId.put(featureId, instance);
                            points.add(point);
                    }
                } catch (JSONException e) {
                    Timber.w("Invalid JSON in instances table: %s", instance.getGeometry());
                }
            }
        }

        if (!viewportInitialized && !points.isEmpty()) {
            mapZoomToBoundingBoxRequested(map);
            viewportInitialized = true;
        }
    }

    public void mapZoomToBoundingBoxRequested(MapFragment map) {
        map.zoomToBoundingBox(points, 0.8, false);
    }

    /**
     * Zooms the map to the new location if the map viewport hasn't been initialized yet.
     */
    public void locationChanged(MapPoint point, MapFragment map) {
        if (!viewportInitialized) {
            map.zoomToPoint(point, true);
            viewportInitialized = true;
        }
    }

    public FeatureStatus getStatusOfClickedFeature(int featureId) {
        Instance instance = instancesByFeatureId.get(featureId);

        if (instance != null) {
            if (instance.getDeletedDate() != null) {
                return FeatureStatus.DELETED;
            }

            if ((instance.getStatus().equals(InstanceProviderAPI.STATUS_COMPLETE)
                    || instance.getStatus().equals(InstanceProviderAPI.STATUS_SUBMITTED)
                    || instance.getStatus().equals(InstanceProviderAPI.STATUS_SUBMISSION_FAILED))
                    && !instance.canEditWhenComplete()) {
                return FeatureStatus.NOT_VIEWABLE;
            } else if (instance.getDatabaseId() != null) {
                if (instance.getStatus().equals(InstanceProviderAPI.STATUS_SUBMITTED)) {
                    return FeatureStatus.VIEW_ONLY;
                }
                return FeatureStatus.EDITABLE;
            }
        }

        return FeatureStatus.UNKNOWN;
    }

    @Nullable
    public Date getDeletedDateOf(int featureId) {
        Instance instance = instancesByFeatureId.get(featureId);
        if (instance != null) {
            return new Date(instance.getDeletedDate());
        } else {
            return null;
        }
    }

    public Long getDatabaseIdOf(int featureId) {
        Instance instance = instancesByFeatureId.get(featureId);
        if (instance != null) {
            return instance.getDatabaseId();
        } else {
            return null;
        }
    }

    private static int getDrawableIdForStatus(String status) {
        switch (status) {
            case InstanceProviderAPI.STATUS_INCOMPLETE:
                return R.drawable.ic_room_blue_24dp;
            case InstanceProviderAPI.STATUS_COMPLETE:
                return R.drawable.ic_room_deep_purple_24dp;
            case InstanceProviderAPI.STATUS_SUBMITTED:
                return R.drawable.ic_room_green_24dp;
            case InstanceProviderAPI.STATUS_SUBMISSION_FAILED:
                return R.drawable.ic_room_red_24dp;
        }
        return R.drawable.ic_map_point;
    }

    public enum FeatureStatus {
        DELETED, NOT_VIEWABLE, VIEW_ONLY, EDITABLE, UNKNOWN
    }
}