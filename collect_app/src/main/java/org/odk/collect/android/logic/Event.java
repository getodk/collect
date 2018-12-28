package org.odk.collect.android.logic;

import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.utilities.EventLogger;

public class Event {
    private final long start;
    public EventLogger.EventTypes eventType;
    public int fecType;
    private final String node;

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

        end = 0;
        endTimeSet = false;
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
            default:
                textValue = "Unknown Event Type: " + eventType;
                break;
        }
        return textValue + "," + node + "," + start + "," + (end != 0 ? end : "");
    }
}
