package org.odk.collect.android.activities.viewmodels;

import android.database.Cursor;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.geo.MapFragment;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public FormMapViewModel(Uri uri) {
        FormsDao dao = new FormsDao();
        form = dao.getFormsFromCursor(dao.getFormsCursor(uri)).get(0);
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

        List<Instance> instances = getInstances();
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

            if (instance.getStatus().equals(InstanceProviderAPI.STATUS_COMPLETE) && !instance.canEditWhenComplete()) {
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

    public Date getDeletedDateOf(int featureId) {
        Instance instance = instancesByFeatureId.get(featureId);
        return new Date(instance.getDeletedDate());
    }

    public Long getDatabaseIdOf(int featureId) {
        Instance instance = instancesByFeatureId.get(featureId);
        return instance.getDatabaseId();
    }

    /**
     * Returns all of the instances matching the current form's form_id, regardless of form version.
     */
    // TODO: move to a repository layer
    private List<Instance> getInstances() {
        InstancesDao dao = new InstancesDao();
        Cursor c = dao.getInstancesCursor(InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " = ?",
                new String[] {form.getJrFormId()});
        return dao.getInstancesFromCursor(c);
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