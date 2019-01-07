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
import org.odk.collect.android.utilities.EventLogger;

public class Event {
    private static final String COMMA = ",";

    private final long start;
    public EventLogger.EventTypes eventType;
    public int fecType;
    private final String node;
    private String latitude;
    private String longitude;
    private String accuracy;
    private boolean collectLocationCoordinates;
    private long end;
    public boolean endTimeSet;

    /*
     * Create a new event
     */
    public Event(long start, EventLogger.EventTypes eventType, int fecType, String node) {
        this.start = start;
        this.eventType = eventType;
        this.fecType = fecType;
        this.node = node;
    }

    /*
     * Return true if this is a view type event
     *  Hierarchy Jump
     *  Question
     *  Prompt for repeat
     */
    public boolean isIntervalViewEvent() {
        return eventType == EventLogger.EventTypes.HIERARCHY || eventType == EventLogger.EventTypes.FEC
                && (fecType == FormEntryController.EVENT_QUESTION
                || fecType == FormEntryController.EVENT_GROUP
                || fecType == FormEntryController.EVENT_END_OF_FORM
                || fecType == FormEntryController.EVENT_PROMPT_NEW_REPEAT);
    }

    /*
     * Mark the end of an interval event
     */
    public void setEnd(long endTime) {
        if (!endTimeSet && isIntervalViewEvent()) {
            this.end = endTime;
            this.endTimeSet = true;
        }
    }

    public void setLocationCoordinates(String latitude, String longitude, String accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        collectLocationCoordinates = true;
    }

    /*
     * convert the event into a record to write to the CSV file
     */
    public String toString() {
        String textValue;
        switch (eventType) {
            case FEC:
                switch (fecType) {
                    case FormEntryController.EVENT_QUESTION:
                        textValue = "question";
                        break;
                    case FormEntryController.EVENT_GROUP:
                        textValue = "group questions";
                        break;
                    case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                        textValue = "add repeat";
                        break;
                    case FormEntryController.EVENT_END_OF_FORM:
                        textValue = "end screen";
                        break;
                    default:
                        textValue = "Unknown FEC: " + fecType;
                }
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

        String log = textValue + COMMA + node + COMMA + start + COMMA + (end != 0 ? end : "");
        if (collectLocationCoordinates) {
            log += COMMA + latitude + COMMA + longitude + COMMA + accuracy;
        }
        return log;
    }
}
