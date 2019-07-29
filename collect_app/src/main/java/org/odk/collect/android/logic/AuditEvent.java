/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.logic;

import androidx.annotation.NonNull;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.utilities.TextUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AuditEvent {

    public enum AuditEventType {
        // Beginning of the form
        BEGINNING_OF_FORM("beginning of form", false, false, false),
        // Create a question
        QUESTION("question", true),
        // Create a group
        GROUP("group questions", true),
        // Prompt to add a new repeat
        PROMPT_NEW_REPEAT("add repeat", true),
        // Repeat group
        REPEAT("repeat", false, false, false),
        // Show the "end of form" view
        END_OF_FORM("end screen", true),
        // Start filling in the form
        FORM_START("form start"),
        // Exit the form
        FORM_EXIT("form exit"),
        // Resume filling in the form after previously exiting
        FORM_RESUME("form resume"),
        // Save the form
        FORM_SAVE("form save"),
        // Finalize the form
        FORM_FINALIZE("form finalize"),
        // Jump to a question
        HIERARCHY("jump", true),
        // Error in save
        SAVE_ERROR("save error"),
        // Error in finalize
        FINALIZE_ERROR("finalize error"),
        // Constraint or missing answer error on save
        CONSTRAINT_ERROR("constraint error"),
        // Delete a repeat group
        DELETE_REPEAT("delete repeat"),

        // Google Play Services are not available
        GOOGLE_PLAY_SERVICES_NOT_AVAILABLE("google play services not available", true, false, true),
        // Location permissions are granted
        LOCATION_PERMISSIONS_GRANTED("location permissions granted", true, false, true),
        // Location permissions are not granted
        LOCATION_PERMISSIONS_NOT_GRANTED("location permissions not granted", true, false, true),
        // Location tracking option is enabled
        LOCATION_TRACKING_ENABLED("location tracking enabled", true, false, true),
        // Location tracking option is disabled
        LOCATION_TRACKING_DISABLED("location tracking disabled", true, false, true),
        // Location providers are enabled
        LOCATION_PROVIDERS_ENABLED("location providers enabled", true, false, true),
        // Location providers are disabled
        LOCATION_PROVIDERS_DISABLED("location providers disabled", true, false, true),
        // Unknown event type
        UNKNOWN_EVENT_TYPE("Unknown AuditEvent Type");

        private final String value;
        private final boolean isLogged;
        private final boolean isInterval;
        private final boolean isLocationRelated;

        AuditEventType(String value, boolean isLogged, boolean isInterval, boolean isLocationRelated) {
            this.value = value;

            this.isLogged = isLogged;
            this.isInterval = isInterval;
            this.isLocationRelated = isLocationRelated;
        }

        AuditEventType(String value, boolean isInterval) {
            this(value, true, isInterval, false);
        }

        AuditEventType(String value) {
            this(value, true, false, false);
        }

        public String getValue() {
            return value;
        }

        public boolean isLogged() {
            return isLogged;
        }

        /**
         * @return true if events of this type have both a start and an end time, false otherwise.
         */
        public boolean isInterval() {
            return isInterval;
        }

        public boolean isLocationRelated() {
            return isLocationRelated;
        }
    }

    private final long start;
    private AuditEventType auditEventType;
    private String latitude;
    private String longitude;
    private String accuracy;
    @NonNull private String oldValue;
    @NonNull private String newValue = "";
    private long end;
    private boolean endTimeSet;
    private boolean isTrackingLocationsEnabled;
    private boolean isTrackingChangesEnabled;
    private FormIndex formIndex;

    /*
     * Create a new event
     */
    public AuditEvent(long start, AuditEventType auditEventType) {
        this(start, auditEventType, false, false, null, null);
    }

    public AuditEvent(long start, AuditEventType auditEventType,  boolean isTrackingLocationsEnabled, boolean isTrackingChangesEnabled) {
        this(start, auditEventType, isTrackingLocationsEnabled, isTrackingChangesEnabled, null, null);
    }

    public AuditEvent(long start, AuditEventType auditEventType, boolean isTrackingLocationsEnabled,
                      boolean isTrackingChangesEnabled, FormIndex formIndex, String oldValue) {
        this.start = start;
        this.auditEventType = auditEventType;
        this.isTrackingLocationsEnabled = isTrackingLocationsEnabled;
        this.isTrackingChangesEnabled = isTrackingChangesEnabled;
        this.formIndex = formIndex;
        this.oldValue = oldValue == null ? "" : oldValue;
    }

    /**
     * @return true if this event's type is an interval event type.
     */
    public boolean isIntervalAuditEventType() {
        return auditEventType.isInterval();
    }

    /*
     * Mark the end of an interval event
     */
    public void setEnd(long endTime) {
        this.end = endTime;
        this.endTimeSet = true;
    }

    public boolean isEndTimeSet() {
        return endTimeSet;
    }

    public AuditEventType getAuditEventType() {
        return auditEventType;
    }

    public FormIndex getFormIndex() {
        return formIndex;
    }

    public boolean hasNewAnswer() {
        return !oldValue.equals(newValue);
    }

    public boolean isLocationAlreadySet() {
        return latitude != null && !latitude.isEmpty()
                && longitude != null && !longitude.isEmpty()
                && accuracy != null && !accuracy.isEmpty();
    }

    public void setLocationCoordinates(String latitude, String longitude, String accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    public void recordValueChange(String newValue) {
        this.newValue = newValue != null ? newValue : "";

        // Clear values if they are equal
        if (this.oldValue.equals(this.newValue)) {
            this.oldValue = "";
            this.newValue = "";
            return;
        }

        if (oldValue.contains(",") || oldValue.contains("\n")) {
            oldValue = getEscapedValueForCsv(oldValue);
        }

        if (this.newValue.contains(",") || this.newValue.contains("\n")) {
            this.newValue = getEscapedValueForCsv(this.newValue);
        }
    }

    /*
     * convert the event into a record to write to the CSV file
     */
    @NonNull
    public String toString() {
        String node = formIndex == null || formIndex.getReference() == null ? "" : getXPathPath(formIndex);

        String event;
        if (isTrackingLocationsEnabled && isTrackingChangesEnabled) {
            event = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", auditEventType.getValue(), node, start, end != 0 ? end : "", latitude, longitude, accuracy, oldValue, newValue);
        } else if (isTrackingLocationsEnabled) {
            event = String.format("%s,%s,%s,%s,%s,%s,%s", auditEventType.getValue(), node, start, end != 0 ? end : "", latitude, longitude, accuracy);
        } else if (isTrackingChangesEnabled) {
            event = String.format("%s,%s,%s,%s,%s,%s", auditEventType.getValue(), node, start, end != 0 ? end : "", oldValue, newValue);
        } else {
            event = String.format("%s,%s,%s,%s", auditEventType.getValue(), node, start, end != 0 ? end : "");
        }

        return event;
    }

    // Get event type based on a Form Controller event
    public static AuditEventType getAuditEventTypeFromFecType(int fcEvent) {
        AuditEventType auditEventType;
        switch (fcEvent) {
            case FormEntryController.EVENT_BEGINNING_OF_FORM:
                auditEventType = AuditEventType.BEGINNING_OF_FORM;
                break;
            case FormEntryController.EVENT_GROUP:
                auditEventType = AuditEventType.GROUP;
                break;
            case FormEntryController.EVENT_REPEAT:
                auditEventType = AuditEventType.REPEAT;
                break;
            case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                auditEventType = AuditEventType.PROMPT_NEW_REPEAT;
                break;
            case FormEntryController.EVENT_END_OF_FORM:
                auditEventType = AuditEventType.END_OF_FORM;
                break;
            default:
                auditEventType = AuditEventType.UNKNOWN_EVENT_TYPE;
        }
        return auditEventType;
    }

    /**
     * Escapes quotes and then wraps in quotes for output to CSV.
     */
    private String getEscapedValueForCsv(String value) {
        if (value.contains("\"")) {
            value = value.replaceAll("\"", "\"\"");
        }

        return "\"" + value + "\"";
    }

    /**
     * Get the XPath path of the node at a particular {@link FormIndex}.
     *
     * Differs from {@link TreeReference#toString()} in that position predicates are only
     * included for repeats. For example, given a group named {@code my-group} that contains a
     * repeat named {@code my-repeat} which in turn contains a question named {@code my-question},
     * {@link TreeReference#toString()} would return paths that look like
     * {@code /my-group[1]/my-repeat[3]/my-question[1]}. In contrast, this method would return
     * {@code /my-group/my-repeat[3]/my-question}.
     *
     * TODO: consider moving to {@link FormIndex}
     */
    private static String getXPathPath(FormIndex formIndex) {
        List<String> nodeNames = new ArrayList<>();
        nodeNames.add(formIndex.getReference().getName(0));

        FormIndex walker = formIndex;
        int i = 1;
        while (walker != null) {
            try {
                String currentNodeName = formIndex.getReference().getName(i);
                if (walker.getInstanceIndex() != -1) {
                    currentNodeName = currentNodeName + "[" + (walker.getInstanceIndex() + 1) + "]";
                }
                nodeNames.add(currentNodeName);
            } catch (IndexOutOfBoundsException e) {
                Timber.i(e);
            }

            walker = walker.getNextLevel();
            i++;
        }
        return "/" + TextUtils.join("/", nodeNames);
    }
}
