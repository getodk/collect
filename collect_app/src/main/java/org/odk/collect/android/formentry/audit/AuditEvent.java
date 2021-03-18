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

package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryController;

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

        CHANGE_REASON("change reason"),

        BACKGROUND_AUDIO_DISABLED("background audio disabled"),

        BACKGROUND_AUDIO_ENABLED("background audio enabled"),

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
    @NonNull
    private String oldValue;
    private String user;
    private final String changeReason;
    @NonNull
    private String newValue = "";
    private long end;
    private boolean endTimeSet;
    private FormIndex formIndex;

    /*
     * Create a new event
     */
    public AuditEvent(long start, AuditEventType auditEventType) {
        this(start, auditEventType, null, null, null, null);
    }

    public AuditEvent(long start, AuditEventType auditEventType,
                      FormIndex formIndex, String oldValue, String user, String changeReason) {
        this.start = start;
        this.auditEventType = auditEventType;
        this.formIndex = formIndex;
        this.oldValue = oldValue == null ? "" : oldValue;
        this.user = user;
        this.changeReason = changeReason;
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

    public void setUser(String user) {
        this.user = user;
    }

    public boolean recordValueChange(String newValue) {
        this.newValue = newValue != null ? newValue : "";

        // Clear values if they are equal
        if (this.oldValue.equals(this.newValue)) {
            this.oldValue = "";
            this.newValue = "";
            return false;
        }

        return true;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public String getUser() {
        return user;
    }

    public long getStart() {
        return start;
    }

    @NonNull
    public String getOldValue() {
        return oldValue;
    }

    @NonNull
    public String getNewValue() {
        return newValue;
    }

    public long getEnd() {
        return end;
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
}
