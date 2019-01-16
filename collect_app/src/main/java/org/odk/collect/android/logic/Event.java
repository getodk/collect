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

import org.javarosa.form.api.FormEntryController;

public class Event {

    public enum EventTypes {
        BEGINNING_OF_FORM,                  // Beginning of the form
        QUESTION,                           // Create a question
        GROUP,                              // Create a group
        PROMPT_NEW_REPEAT,                  // Prompt do add a new group
        REPEAT,                             // Repeat group
        END_OF_FORM,                        // Show the "end of form" view
        FORM_START,                         // Start filling in the form
        FORM_EXIT,                          // Exit the form
        FORM_RESUME,                        // Resume filling in the form after previously exiting
        FORM_SAVE,                          // Save the form
        FORM_FINALIZE,                      // Finalize the form
        HIERARCHY,                          // Jump to a question
        SAVE_ERROR,                         // Error in save
        FINALIZE_ERROR,                     // Error in finalize
        CONSTRAINT_ERROR,                   // Constraint or missing answer error on save
        DELETE_REPEAT,                      // Delete a repeat group
        GOOGLE_PLAY_SERVICES_NOT_AVAILABLE, // Google Play Services are not available
        LOCATION_PERMISSIONS_GRANTED,       // Location permissions are granted
        LOCATION_PERMISSIONS_NOT_GRANTED,   // Location permissions are not granted
        BACKGROUND_LOCATION_ENABLED,        // Background location option is enabled
        BACKGROUND_LOCATION_DISABLED,       // Background location option is disabled
        LOCATION_PROVIDERS_ENABLED,         // Location providers are enabled
        LOCATION_PROVIDERS_DISABLED         // Location providers are disabled
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
    public String toString() {
        String textValue;
        switch (eventType) {
            case QUESTION:
                textValue = "question";
                break;
            case GROUP:
                textValue = "group questions";
                break;
            case PROMPT_NEW_REPEAT:
                textValue = "add repeat";
                break;
            case END_OF_FORM:
                textValue = "end screen";
                break;
            case FORM_START:
                textValue = "form start";
                break;
            case FORM_EXIT:
                textValue = "form exit";
                break;
            case FORM_RESUME:
                textValue = "form resume";
                break;
            case FORM_SAVE:
                textValue = "form save";
                break;
            case FORM_FINALIZE:
                textValue = "form finalize";
                break;
            case HIERARCHY:
                textValue = "jump";
                break;
            case SAVE_ERROR:
                textValue = "save error";
                break;
            case FINALIZE_ERROR:
                textValue = "finalize error";
                break;
            case CONSTRAINT_ERROR:
                textValue = "constraint error";
                break;
            case DELETE_REPEAT:
                textValue = "delete repeat";
                break;
            case GOOGLE_PLAY_SERVICES_NOT_AVAILABLE:
                textValue = "google play services not available";
                break;
            case LOCATION_PERMISSIONS_GRANTED:
                textValue = "location permissions granted";
                break;
            case LOCATION_PERMISSIONS_NOT_GRANTED:
                textValue = "location permissions not granted";
                break;
            case BACKGROUND_LOCATION_ENABLED:
                textValue = "background location enabled";
                break;
            case BACKGROUND_LOCATION_DISABLED:
                textValue = "background location disabled";
                break;
            case LOCATION_PROVIDERS_ENABLED:
                textValue = "location providers enabled";
                break;
            case LOCATION_PROVIDERS_DISABLED:
                textValue = "location providers disabled";
                break;
            default:
                textValue = "Unknown Event Type: " + eventType;
                break;
        }

        return hasLocation()
                ? String.format("%s,%s,%s,%s,%s,%s,%s", textValue, node, start, end != 0 ? end : "", latitude, longitude, accuracy)
                : String.format("%s,%s,%s,%s", textValue, node, start, end != 0 ? end : "");
    }

    public static EventTypes getEventType(int event) {
        EventTypes eventType = null;
        switch (event) {
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
        }
        return eventType;
    }
}
