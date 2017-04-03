
package org.odk.collect.android.utilities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.tasks.TimerSaveTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Handle logging of timer events and pass them to an Async task to append to a file
 */
public class TimerLogger {

    public enum EventTypes {
        FEC,                // FEC, Real type defined in FormEntryController
        FORM_START,         // Start filling in the form
        FORM_EXIT,          // Exit the form
        FORM_RESUME,        // Resume filling in the form after previously exiting
        FORM_SAVE,          // Save the form
        FORM_FINALIZE,      // Finalize the form
        HIERARCHY,
        PREFERENCES,
        LANGUAGE,
        VIEW_END,
        NON_VIEW_END
    }

    public class Event {

        long start;
        EventTypes eventType;
        int fecType;
        String node;

        long end;
        boolean endTimeSet;

        private class EventDetails {
            boolean hasIntervalTime;
            String name;
        }

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
         * Return true if this is a non view type event
         *  Non View events occur inside view events
         */
        public boolean isNonViewIntervalEvent() {
            if (eventType == EventTypes.LANGUAGE || eventType == EventTypes.PREFERENCES) {
                return true;
            }
            return false;
        }

        /*
         * Return true if this is a view type event
         *  Hierarchy Jump
         *  Question
         *  Prompt for repeat
         */
        public boolean isIntervalViewEvent() {
            if (eventType == EventTypes.HIERARCHY || (eventType == EventTypes.FEC &&
                    (fecType == FormEntryController.EVENT_QUESTION ||
                            fecType == FormEntryController.EVENT_PROMPT_NEW_REPEAT))) {
                return true;
            }
            return false;
        }

        /*
         * Mark the end of an interval event
         */
        public void setEnd(EventTypes endType, long endTime) {

            if (!endTimeSet) {
                if (endType == EventTypes.NON_VIEW_END && isNonViewIntervalEvent() || endType == EventTypes.VIEW_END && isIntervalViewEvent()) {
                    this.end = endTime;
                    this.endTimeSet = true;
                }
            }

        }

        /*
         * Return true if the event can be saved to the timer log
         * This will return false if at least one of the events is waiting for an end time
         */
        public boolean canSave() {
            if (this.endTimeSet == false && (isNonViewIntervalEvent() || isIntervalViewEvent())) {
                return false;
            }
            return true;
        }

        /*
         * Return true if the event is a currently open interval view event
         */
        public boolean inIntervalView() {

            if (this.endTimeSet == false && isIntervalViewEvent()) {
                return true;
            }

            return false;
        }

        /*
         * convert the event into a record to write to the CSV file
         */
        public String toString() {
            String sType = "unknown";
            switch (eventType) {
                case FEC:
                    switch (fecType) {
                        case FormEntryController.EVENT_QUESTION:
                            sType = "question";
                            break;
                        case FormEntryController.EVENT_GROUP:
                            sType = "group questions";
                            break;
                        case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                            sType = "prompt repeat";
                            break;
                        case FormEntryController.EVENT_BEGINNING_OF_FORM:
                            sType = "begin";
                            break;
                        case FormEntryController.EVENT_END_OF_FORM:
                            sType = "end";
                            break;
                        default:
                            sType = "Unknown FEC: " + fecType;
                    }
                    break;
                case FORM_START:
                    sType = "form start";
                    break;
                case FORM_EXIT:
                    sType = "form exit";
                    break;
                case FORM_RESUME:
                    sType = "form resume";
                    break;
                case FORM_SAVE:
                    sType = "form save";
                    break;
                case FORM_FINALIZE:
                    sType = "form finalize";
                    break;
                case HIERARCHY:
                    sType = "jump";
                    break;
                case PREFERENCES:
                    sType = "preferences menu";
                    break;
                case LANGUAGE:
                    sType = "language menu";
                    break;
                default:
                    sType = "Unknown Event Type: " + eventType;
                    break;
            }
            return sType + "," + node + "," + String.valueOf(start) + ","
                    + (end != 0 ? String.valueOf(end) : "");
        }
    }

    private final static String t = "TimerLogger";
    private static AsyncTask saveTask = null;
    private ArrayList<Event> mEvents = null;
    private String filename = null;
    private File timerlogFile = null;
    private long surveyOpenTime = 0;
    private long surveyOpenElapsedTime = 0;
    private boolean mTimerEnabled = false;              // Set true of the timer logger is enabled
    private boolean locationRecordingEnabled = false;   // Set true to also record gps coordinates


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

    public void logTimerEvent(EventTypes eventType, int fecType, TreeReference ref) {

        if (mTimerEnabled) {
            // Calculate the time and add the event to the events array
            long start = getEventTime();
            String node = ref == null ? "" : ref.toString();

            Event newEvent = new Event(start, eventType, fecType, node);

            // Ignore the event if we are already in an interval view event ie the question has been refreshed
            if (newEvent.isIntervalViewEvent()) {
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).isIntervalViewEvent() && !mEvents.get(i).endTimeSet) {
                        return;
                    }
                }
            }
            mEvents.add(newEvent);

            // If the user is exiting then mark any open questions as closed
            if (eventType == EventTypes.FORM_EXIT || eventType == EventTypes.FORM_FINALIZE) {
                exitView(EventTypes.VIEW_END);
            }
            writeEvents();
        }

    }

    /*
     * Exit a question, repeat dialog, language select etc
     */
    public void exitView(EventTypes endType) {

        if (mTimerEnabled) {

            // Calculate the time and add the event to the events array
            long end = getEventTime();
            for (int i = 0; i < mEvents.size(); i++) {
                mEvents.get(i).setEnd(endType, end);
            }
            writeEvents();
        }
    }

    private void writeEvents() {

        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {

            // Verify that all the pending events are ready to send, may require us to wait for an "exit" event
            boolean canSave = true;
            for (int i = 0; i < mEvents.size(); i++) {
                Event pe = mEvents.get(i);
                if (!pe.canSave()) {
                    canSave = false;
                    break;
                }
            }

            if (canSave) {
                Event[] eArray = mEvents.toArray(new Event[mEvents.size()]);
                saveTask = new TimerSaveTask(timerlogFile).execute(eArray);
                mEvents = new ArrayList<Event>();
            } else {
                Log.e(t, "Queueing Timer Event");
            }

        } else {
            Log.e(t, "Queueing Timer Event");
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