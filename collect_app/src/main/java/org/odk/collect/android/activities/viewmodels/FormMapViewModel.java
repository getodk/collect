package org.odk.collect.android.activities.viewmodels;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * This is only being used to perform logic for another ViewModel
 * ({@link org.odk.collect.geo.SelectionMapViewModel}) so there is no reason for the code here
 * to require a {@link ViewModel} implementation.
 */
@Deprecated
public class FormMapViewModel extends ViewModel {
    /**
     * The form that is mapped.
     */
    private final Form form;

    /**
     * The count of all filled instances of this form, including unmappable ones.
     */
    private int totalInstanceCount;

    /**
     * The filled instances of this form that can be mapped.
     */
    private List<MappableFormInstance> mappableFormInstances;

    private final InstancesRepository instancesRepository;

    public FormMapViewModel(Form form, InstancesRepository instancesRepository) {
        this.instancesRepository = instancesRepository;
        this.form = form;
    }

    /**
     * Returns the count of all filled instances of this form, including unmappable ones.
     */
    public int getTotalInstanceCount() {
        initializeFormInstances();
        return totalInstanceCount;
    }

    private void initializeFormInstances() {
        List<Instance> instances = instancesRepository.getAllByFormId(form.getFormId());

        // Ideally we could observe database changes instead of re-computing this every time.
        totalInstanceCount = instances.size();
        mappableFormInstances = getMappableFormInstances(instances);
    }

    /**
     * Returns a list of filled instances of this form that can be mapped.
     */
    public List<MappableFormInstance> getMappableFormInstances() {
        initializeFormInstances();
        return mappableFormInstances;
    }

    private List<MappableFormInstance> getMappableFormInstances(List<Instance> allInstances) {
        List<MappableFormInstance> mappableFormInstances = new ArrayList<>();
        for (Instance instance : allInstances) {
            if (instance.getGeometry() != null) {
                try {
                    JSONObject geometry = new JSONObject(instance.getGeometry());
                    switch (instance.getGeometryType()) {
                        case "Point":
                            JSONArray coordinates = geometry.getJSONArray("coordinates");
                            // In GeoJSON, longitude comes before latitude.
                            Double lon = coordinates.getDouble(0);
                            Double lat = coordinates.getDouble(1);

                            mappableFormInstances.add(new MappableFormInstance(
                                    instance.getDbId(),
                                    lat, lon,
                                    instance.getDisplayName(),
                                    instance.getLastStatusChangeDate(),
                                    instance.getStatus(),
                                    getClickActionForInstance(instance)
                            ));
                    }
                } catch (JSONException e) {
                    Timber.w("Invalid JSON in instances table: %s", instance.getGeometry());
                }
            }
        }

        return mappableFormInstances;
    }

    private ClickAction getClickActionForInstance(Instance instance) {
        if (instance != null) {
            if (instance.getDeletedDate() != null) {
                return ClickAction.DELETED_TOAST;
            }

            if ((instance.getStatus().equals(Instance.STATUS_COMPLETE)
                    || instance.getStatus().equals(Instance.STATUS_SUBMITTED)
                    || instance.getStatus().equals(Instance.STATUS_SUBMISSION_FAILED))
                    && !instance.canEditWhenComplete()) {
                return ClickAction.NOT_VIEWABLE_TOAST;
            } else if (instance.getDbId() != null) {
                if (instance.getStatus().equals(Instance.STATUS_SUBMITTED)
                        || instance.getStatus().equals(Instance.STATUS_SUBMISSION_FAILED)) {
                    return ClickAction.OPEN_READ_ONLY;
                }
                return ClickAction.OPEN_EDIT;
            }
        }

        return ClickAction.NONE;
    }

    public long getDeletedDateOf(long databaseId) {
        return instancesRepository.get(databaseId).getDeletedDate();
    }

    public enum ClickAction {
        DELETED_TOAST, NOT_VIEWABLE_TOAST, OPEN_READ_ONLY, OPEN_EDIT, NONE
    }

    public class MappableFormInstance {
        private final long databaseId;
        private final Double latitude;
        private final Double longitude;
        private final String instanceName;
        private final Long lastStatusChangeDate;
        private final String status;
        private final ClickAction clickAction;

        MappableFormInstance(long databaseId, Double latitude, Double longitude, String instanceName,
                             Long lastStatusChangeDate, String status, ClickAction clickAction) {
            this.databaseId = databaseId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.instanceName = instanceName;
            this.lastStatusChangeDate = lastStatusChangeDate;
            this.status = status;
            this.clickAction = clickAction;
        }

        public long getDatabaseId() {
            return databaseId;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public Date getLastStatusChangeDate() {
            return new Date(lastStatusChangeDate);
        }

        public String getStatus() {
            return status;
        }

        public ClickAction getClickAction() {
            return clickAction;
        }
    }
}
