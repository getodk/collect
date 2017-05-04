
package org.odk.collect.android.utilities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.tasks.TimerSaveTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Handle logging of timer events and pass them to an Async task to append to a file
 * <p>
 * Notes:
 * 1) If the user has saved the form, resumes editing, then exits without saving then the timing data during the
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
        HIERARCHY
    }

    public class Event {

        long start;
        EventTypes eventType;
        int fecType;
        String node;
        String dirn;

        long end;
        boolean endTimeSet;

        private class EventDetails {
            boolean hasIntervalTime;
            String name;
        }

        /*
         * Create a new event
         */
        Event(long start, EventTypes eventType, int fecType, String node, boolean advancingPage) {
            this.start = start;
            this.eventType = eventType;
            this.fecType = fecType;
            this.node = node;

            if (eventType == EventTypes.FEC
                    && (fecType == FormEntryController.EVENT_QUESTION
                    || fecType == FormEntryController.EVENT_GROUP)) {
                this.dirn = advancingPage ? "fwd" : "back";
            } else {
                this.dirn = "";
            }

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
            if (eventType == EventTypes.HIERARCHY || (eventType == EventTypes.FEC
                    && (fecType == FormEntryController.EVENT_QUESTION
                    || fecType == FormEntryController.EVENT_GROUP
                    || fecType == FormEntryController.EVENT_PROMPT_NEW_REPEAT))) {
                return true;
            }
            return false;
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
            String textValue = null;
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
                        case FormEntryController.EVENT_REPEAT:
                            textValue = "delete repeat";
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
                default:
                    textValue = "Unknown Event Type: " + eventType;
                    break;
            }
            return textValue + "," + node + "," + String.valueOf(start) + ","
                    + (end != 0 ? String.valueOf(end) : "") + ","
                    + dirn;
        }
    }

    private static AsyncTask saveTask = null;
    private ArrayList<Event> mEvents = null;
    private String filename = null;
    private File timerlogFile = null;
    private long surveyOpenTime = 0;
    private long surveyOpenElapsedTime = 0;
    private boolean mTimerEnabled = false;              // Set true of the timer logger is enabled


    public TimerLogger(File instanceFile, SharedPreferences sharedPreferences, FormController formController) {

        /*
         * The timer logger is enabled if:
         *  1) The meta section of the form contains a logging entry
         *      <orx:logging />
         *  2) And logging has been enabled in the device preferences
         */
        boolean loggingEnabledInForm = formController.getSubmissionMetadata().logging;
        boolean loggingEnabledInPref = sharedPreferences.getBoolean(
                AdminKeys.KEY_TIMER_LOG_ENABLED, false);
        mTimerEnabled = loggingEnabledInForm && loggingEnabledInPref;

        if (mTimerEnabled) {
            filename = "timing.csv";
            if (instanceFile != null) {
                File instanceFolder = instanceFile.getParentFile();
                timerlogFile = new File(instanceFolder.getPath() + File.separator + filename);
            }
            mEvents = new ArrayList<Event>();
        }
    }


    public void setPath(String instancePath) {
        if (mTimerEnabled) {
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

        if (mTimerEnabled) {

            // For any existing interval events, calculate the end time
            long end = getEventTime();
            for (int i = 0; i < mEvents.size(); i++) {
                mEvents.get(i).setEnd(end);
            }

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

            Event newEvent = new Event(start, eventType, fecType, node, advancingPage);

            /*
             * Ignore the event if we are already in an interval view event
             * This can happen if the user is on a question page and the page gets refreshed
             */
            if (newEvent.isIntervalViewEvent()) {
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).isIntervalViewEvent() && !mEvents.get(i).endTimeSet) {
                        return;
                    }
                }
            }

            /*
             * Ignore beginning of form events
             */
            if (newEvent.eventType == EventTypes.FEC.FEC
                    && newEvent.fecType == FormEntryController.EVENT_BEGINNING_OF_FORM) {
                return;
            }

            mEvents.add(newEvent);

            if (writeImmediatelyToDisk) {
                writeEvents();
            }
        }

    }

    /*
     * Exit a question
     */
    public void exitView() {

        if (mTimerEnabled) {

            // Calculate the time and add the event to the events array
            long end = getEventTime();
            for (int i = 0; i < mEvents.size(); i++) {
                mEvents.get(i).setEnd(end);
            }

            writeEvents();
        }
    }

    private void writeEvents() {

        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {

            Event[] eventArray = mEvents.toArray(new Event[mEvents.size()]);
            saveTask = new TimerSaveTask(timerlogFile).execute(eventArray);
            mEvents = new ArrayList<Event>();

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

        // debug
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateTime = sdf.format(surveyOpenTime + (SystemClock.elapsedRealtime() - surveyOpenElapsedTime));

        return surveyOpenTime + (SystemClock.elapsedRealtime() - surveyOpenElapsedTime);
    }

}