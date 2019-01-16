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

import android.support.annotation.NonNull;

import org.javarosa.form.api.FormEntryController;

public class Event {

    public enum EventTypes {
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
        BACKGROUND_LOCATION_ENABLED("background location enabled"),                 // Background location option is enabled
        BACKGROUND_LOCATION_DISABLED("background location disabled"),               // Background location option is disabled
        LOCATION_PROVIDERS_ENABLED("location providers enabled"),                   // Location providers are enabled
        LOCATION_PROVIDERS_DISABLED("location providers disabled"),                 // Location providers are disabled
        UNKNOWN_EVENT_TYPE("Unknown Event Type");                                   // Unknown event type

        private final String value;

        EventTypes(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final long start;
    public EventTypes eventType;
    private final String node;
    private String latitude;
    private String longitude;
    private String accuracy;
    private long end;
    public boolean endTimeSet;

    /*
     * Create a new event
     */
    public Event(long start, EventTypes eventType, String node) {
        this.start = start;
        this.eventType = eventType;
        this.node = node;
    }

    /*
     * Return true if this is a view type event
     *  Hierarchy Jump
     *  Question
     *  Prompt for repeat
     */
    public boolean isIntervalViewEvent() {
        return eventType == EventTypes.HIERARCHY
                || eventType == EventTypes.QUESTION
                || eventType == EventTypes.GROUP
                || eventType == EventTypes.END_OF_FORM
                || eventType == EventTypes.PROMPT_NEW_REPEAT;
    }

    /*
     * Mark the end of an interval event
     */
    public void setEnd(long endTime) {
        this.end = endTime;
        this.endTimeSet = true;
    }

    public boolean hasLocation() {
        return latitude != null && !latitude.isEmpty()
                && longitude != null && !longitude.isEmpty()
                && accuracy != null && !accuracy.isEmpty();
    }

    public void setLocationCoordinates(String latitude, String longitude, String accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    /*
     * convert the event into a record to write to the CSV file
     */
    @NonNull
    public String toString() {
        return hasLocation()
                ? String.format("%s,%s,%s,%s,%s,%s,%s", eventType.getValue(), node, start, end != 0 ? end : "", latitude, longitude, accuracy)
                : String.format("%s,%s,%s,%s", eventType.getValue(), node, start, end != 0 ? end : "");
    }

    // Get event type based on a Form Controller event
    public static EventTypes getEventType(int fcEvent) {
        EventTypes eventType;
        switch (fcEvent) {
            case FormEntryController.EVENT_BEGINNING_OF_FORM:
                eventType = EventTypes.BEGINNING_OF_FORM;
                break;
            case FormEntryController.EVENT_QUESTION:
                eventType = EventTypes.QUESTION;
                break;
            case FormEntryController.EVENT_GROUP:
                eventType = EventTypes.GROUP;
                break;
            case FormEntryController.EVENT_REPEAT:
                eventType = EventTypes.REPEAT;
                break;
            case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                eventType = EventTypes.PROMPT_NEW_REPEAT;
                break;
            case FormEntryController.EVENT_END_OF_FORM:
                eventType = EventTypes.END_OF_FORM;
                break;
            default:
                eventType = EventTypes.UNKNOWN_EVENT_TYPE;
        }
        return eventType;
    }
}
