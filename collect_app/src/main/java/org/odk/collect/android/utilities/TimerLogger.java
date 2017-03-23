/*
 * Copyright 2012 Google Inc.
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
package org.odk.collect.android.utilities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.tasks.TimerSaveTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Handle logging of timer events and pass them to an Async task to append to a file
 */
public class TimerLogger {

    public class Event {

        long start;
        int eventType;
        int fecType;
        String node;

        long end;
        boolean endTimeSet;

        // Valid event types
        public static final int FEC = 0;
        public static final int START = 1001;   // Start from 1001 to not clash with FEC events
        public static final int STOP = 1002;
        public static final int RESUME = 1003;
        public static final int FINALIZE = 1004;
        public static final int HIERARCHY = 1005;
        public static final int PREFERENCES = 1006;
        public static final int LANGUAGE = 1007;
        public static final int VIEW_END = 2000;     // The end event for Questions, repeats, Hierarchy jumps etc
        public static final int NON_VIEW_END = 2001;    // The end event for select language which can be embedded within a question


        private class EventDetails {
            boolean hasIntervalTime;
            String name;
        }

        /*
         * Create a new event
         */
        Event(long start, int eventType, int fecType, String node) {
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
            if (eventType == LANGUAGE || eventType == PREFERENCES) {
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
            if (eventType == HIERARCHY || (eventType == FEC &&
                    (fecType == FormEntryController.EVENT_QUESTION ||
                            fecType == FormEntryController.EVENT_PROMPT_NEW_REPEAT))) {
                return true;
            }
            return false;
        }

        /*
         * Mark the end of an interval event
         */
        public void setEnd(int endType, long endTime) {

            if (!endTimeSet) {
                if (endType == NON_VIEW_END && isNonViewIntervalEvent() || endType == VIEW_END && isIntervalViewEvent()) {
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
                case START:
                    sType = "start";
                    break;
                case STOP:
                    sType = "stop";
                    break;
                case RESUME:
                    sType = "resume";
                    break;
                case FINALIZE:
                    sType = "finalize";
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
    private boolean mTimerEnabled = false;               // Set true of the timer logger is enabled
    private boolean locationRecordingEnabled = false;   // Set true to also record gps coordinates


    public TimerLogger(File instanceFile, SharedPreferences sharedPreferences) {

        /*
         * The timer logger is enabled if:
         *   - The timer log has been enabled in the administration properties of collect
         */
        mTimerEnabled = sharedPreferences.getBoolean(
                PreferencesActivity.KEY_TIMER_LOG_ENABLED, false);

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

    public void logTimerEvent(int eventType, int fecType, TreeReference ref) {

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
            if (eventType == Event.STOP || eventType == Event.FINALIZE) {
                exitView(Event.VIEW_END);
            }
            writeEvents();
        }

    }

    /*
     * Exit a question, repeat dialog, language select etc
     */
    public void exitView(int endType) {

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