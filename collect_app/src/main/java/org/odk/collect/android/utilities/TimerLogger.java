
package org.odk.collect.android.utilities;

import android.os.AsyncTask;
import android.os.SystemClock;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.TimerSaveTask;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

import static org.odk.collect.android.logic.FormController.AUDIT_FILE_NAME;

/**
 * Handle logging of timer events and pass them to an Async task to append to a file
 * Notes:
 * 1) If the user has saved the form, then resumes editing, then exits without saving then the timing data during the
 * second editing session will be saved.  This is OK as it records user activity.  However if the user exits
 * without saving and they have never saved the form then the timing data is lost as the form editing will be
 * restarted from scratch.
 * 2) The times for questions in a group are not shown.  Only the time for the group is shown.
 */
public class TimerLogger {

    public enum EventTypes {
        FEC,                // FEC, Real type defined in FormEntryController
        FORM_START,         // Start filling in the form
        FORM_EXIT,          // Exit the form
        FORM_RESUME,        // Resume filling in the form after previously exiting
        FORM_SAVE,          // Save the form
        FORM_FINALIZE,      // Finalize the form
        HIERARCHY,          // Jump to a question
        SAVE_ERROR,         // Error in save
        FINALIZE_ERROR,     // Error in finalize
        CONSTRAINT_ERROR,   // Constraint or missing answer error on save
        DELETE_REPEAT       // Delete a repeat group
    }

    public class Event {

        long start;
        EventTypes eventType;
        int fecType;
        String node;

        long end;
        boolean endTimeSet;

        /*
         * Create a new event
         */
        Event(long start, EventTypes eventType, int fecType, String node) {
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
        private boolean isIntervalViewEvent() {
            return eventType == EventTypes.HIERARCHY || eventType == EventTypes.FEC
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
            return textValue + "," + node + "," + start + ","
                    + (end != 0 ? end : "");
        }
    }

    private static AsyncTask saveTask;
    private ArrayList<Event> events;
    private String filename;
    private File timerlogFile;
    private long surveyOpenTime;
    private long surveyOpenElapsedTime;
    private final boolean timerEnabled;              // Set true of the timer logger is enabled


    public TimerLogger(File instanceFile, FormController formController) {

        /*
         * The timer logger is enabled if the meta section of the form contains a logging entry
         *      <orx:audit />
         */
        timerEnabled = formController.getSubmissionMetadata().audit;

        if (timerEnabled) {
            filename = AUDIT_FILE_NAME;
            if (instanceFile != null) {
                File instanceFolder = instanceFile.getParentFile();
                timerlogFile = new File(instanceFolder.getPath() + File.separator + filename);
            }
            events = new ArrayList<>();
        }
    }


    public void setPath(String instancePath) {
        if (timerEnabled) {
            timerlogFile = new File(instancePath + File.separator + filename);
        }
    }

    /*
     * Log a new event
     */
    public void logTimerEvent(EventTypes eventType,
                              int fecType,
                              TreeReference ref,
                              boolean advancingPage,
                              boolean writeImmediatelyToDisk) {

        if (timerEnabled) {

            Timber.i("Event recorded: %s : %s", eventType, fecType);
            // Calculate the time and add the event to the events array
            long start = getEventTime();

            // Set the node value from the question reference
            String node = ref == null ? "" : ref.toString();
            if (node != null && eventType == EventTypes.FEC
                    && (fecType == FormEntryController.EVENT_QUESTION
                    || fecType == FormEntryController.EVENT_GROUP)) {
                int idx = node.lastIndexOf('[');
                if (idx > 0) {
                    node = node.substring(0, idx);
                }
            }

            Event newEvent = new Event(start, eventType, fecType, node);

            /*
             * Close any existing interval events if the view is being exited
             */
            if (newEvent.eventType == EventTypes.FORM_EXIT) {
                for (Event ev : events) {
                    ev.setEnd(start);
                }
            }

            /*
             * Ignore the event if we are already in an interval view event or have jumped
             * This can happen if the user is on a question page and the page gets refreshed
             * The exception is hierarchy events since they interrupt an existing interval event
             */
            if (newEvent.isIntervalViewEvent()) {
                for (Event ev : events) {
                    if (ev.isIntervalViewEvent() && !ev.endTimeSet) {
                        return;
                    }
                }
            }

            /*
             * Ignore beginning of form events and repeat events
             */
            if (newEvent.eventType == EventTypes.FEC
                    && (newEvent.fecType == FormEntryController.EVENT_BEGINNING_OF_FORM
                    || newEvent.fecType == FormEntryController.EVENT_REPEAT)) {
                return;
            }

            /*
             * Having got to this point we are going to keep the event
             */
            events.add(newEvent);

            /*
             * Write the event unless it is an interval event in which case we need to wait for the end of that event
             */
            if (writeImmediatelyToDisk && !newEvent.isIntervalViewEvent()) {
                writeEvents();
            }
        }

    }

    /*
     * Exit a question
     */
    public void exitView() {

        if (timerEnabled) {

            // Calculate the time and add the event to the events array
            long end = getEventTime();
            for (Event ev : events) {
                ev.setEnd(end);
            }

            writeEvents();
        }
    }

    private void writeEvents() {

        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {

            Event[] eventArray = events.toArray(new Event[events.size()]);
            if (timerlogFile != null) {
                saveTask = new TimerSaveTask(timerlogFile).execute(eventArray);
            } else {
                Timber.e("timerlogFile null when attempting to write events.");
            }
            events = new ArrayList<>();

        } else {
            Timber.i("Queueing Timer Event");
        }
    }

    /*
     * Use the time the survey was opened as a consistent value for wall clock time
     */
    private long getEventTime() {
        if (surveyOpenTime == 0) {
            surveyOpenTime = System.currentTimeMillis();
            surveyOpenElapsedTime = SystemClock.elapsedRealtime();
        }

        return surveyOpenTime + (SystemClock.elapsedRealtime() - surveyOpenElapsedTime);
    }

}