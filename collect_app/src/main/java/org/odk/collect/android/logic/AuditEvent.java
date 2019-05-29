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
import org.javarosa.form.api.FormEntryController;

public class AuditEvent {

    public enum AuditEventType {
        BEGINNING_OF_FORM("beginning of form"),                                     // Beginning of the form
        QUESTION("question"),                                                       // Create a question
        GROUP("group questions"),                                                   // Create a group
        PROMPT_NEW_REPEAT("add repeat"),                                            // Prompt do add a new group
        REPEAT("repeat"),                                                           // Repeat group
        END_OF_FORM("end screen"),                                                  // Show the "end of form" view
        FORM_START("form start"),                                                   // Start filling in the form
        FORM_EXIT("form exit"),                                                     // Exit the form
        FORM_RESUME("form resume"),                                                 // Resume filling in the form after previously exiting
        FORM_SAVE("form save"),                                                     // Save the form
        FORM_FINALIZE("form finalize"),                                             // Finalize the form
        HIERARCHY("jump"),                                                          // Jump to a question
        SAVE_ERROR("save error"),                                                   // Error in save
        FINALIZE_ERROR("finalize error"),                                           // Error in finalize
        CONSTRAINT_ERROR("constraint error"),                                       // Constraint or missing answer error on save
        DELETE_REPEAT("delete repeat"),                                             // Delete a repeat group
        GOOGLE_PLAY_SERVICES_NOT_AVAILABLE("google play services not available"),   // Google Play Services are not available
        LOCATION_PERMISSIONS_GRANTED("location permissions granted"),               // Location permissions are granted
        LOCATION_PERMISSIONS_NOT_GRANTED("location permissions not granted"),       // Location permissions are not granted
        LOCATION_TRACKING_ENABLED("location tracking enabled"),                     // Location tracking option is enabled
        LOCATION_TRACKING_DISABLED("location tracking disabled"),                   // Location tracking option is disabled
        LOCATION_PROVIDERS_ENABLED("location providers enabled"),                   // Location providers are enabled
        LOCATION_PROVIDERS_DISABLED("location providers disabled"),                 // Location providers are disabled
        UNKNOWN_EVENT_TYPE("Unknown AuditEvent Type");                              // Unknown event type

        private final String value;

        AuditEventType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
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

    /*
     * Return true if this is a view type event
     *  Hierarchy Jump
     *  Question
     *  Prompt for repeat
     */
    public boolean isIntervalAuditEventType() {
        return auditEventType == AuditEventType.HIERARCHY
                || auditEventType == AuditEventType.QUESTION
                || auditEventType == AuditEventType.GROUP
                || auditEventType == AuditEventType.END_OF_FORM
                || auditEventType == AuditEventType.PROMPT_NEW_REPEAT;
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
        String node = formIndex == null || formIndex.getReference() == null ? "" : formIndex.getReference().toString();
        if (auditEventType == AuditEvent.AuditEventType.QUESTION || auditEventType == AuditEvent.AuditEventType.GROUP) {
            int idx = node.lastIndexOf('[');
            if (idx > 0) {
                node = node.substring(0, idx);
            }
        }

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
}
