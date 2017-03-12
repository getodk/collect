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

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.tasks.TimerSaveTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        boolean hasIntervalTime;
        boolean endTimeSet;

        // Valid event types
        public static final int FEC = 1000;            // Start from 1000 to not clash with FEC events
        public static final int START = 1001;
        public static final int STOP = 1002;
        public static final int RESUME = 1003;
        public static final int FINALIZE = 1004;

        Event(long start, int eventType, int fecType, String node, boolean hasIntervalTime) {
            this.start = start;
            this.eventType = eventType;
            this.fecType = fecType;
            this.node = node;
            this.hasIntervalTime = hasIntervalTime;
        }

        public void setEnd(long end) {
            this.end = end;
            this.endTimeSet = true;
        }

        public String toString() {
            String sType = "unknown";
            switch (eventType) {
                case FEC:
                    switch(fecType) {
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
                default:
                    sType = "Unknown Event Type: " + eventType;
                    break;
            }
            return sType + "," + node + "," + String.valueOf(start) + ","
                    + (hasIntervalTime ? String.valueOf(end) : "");
        }
    }

    private final static String t = "TimerLogger";
    private static AsyncTask saveTask = null;
    private ArrayList<Event> mEvents = null;
    private String filename = "timerlog.csv";
    private File timerlogFile = null;
    private long surveyOpenTime = 0;
    private long surveyOpenElapsedTime = 0;


    public TimerLogger(File instancePath) {
        if(instancePath != null ) {
            File instanceFolder = instancePath.getParentFile();
            timerlogFile = new File(instanceFolder.getPath() + File.separator + filename);
        }
        mEvents = new ArrayList<Event>();
    }


    public void setPath(String instancePath) {
        timerlogFile =  new File(instancePath + File.separator + filename);
    }

    public void logTimerEvent(int eventType, int fecType, TreeReference ref) {

        // Calculate the time and add the event to the events array
        long start = getEventTime();
        String node = ref == null ? "" : ref.toString();

        Log.e(t, "######### Timer Event: " + eventType + " : " + fecType);

        boolean hasIntervalTime = (eventType == TimerLogger.Event.FEC &&
                (fecType == FormEntryController.EVENT_QUESTION ||
                fecType == FormEntryController.EVENT_PROMPT_NEW_REPEAT));

        mEvents.add(new Event(start, eventType, fecType, node, hasIntervalTime));

        // If the user is exiting then mark any open questions as closed
        if(eventType == Event.STOP || eventType == Event.FINALIZE) {
            exitView();
        }
        writeEvents();

    }

    public void exitView() {

        Log.e(t, "######### Exit view");

        // Calculate the time and add the event to the events array
        long end = getEventTime();
        for (int i = 0; i < mEvents.size(); i++) {
            mEvents.get(i).setEnd(end);
        }
        writeEvents();
    }

    private void writeEvents() {
        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {

            // Verify that all the pending events are ready to send, may require us to wait for an "exit" event
            boolean canSend = true;
            for (int i = 0; i < mEvents.size(); i++) {
                Event pe = mEvents.get(i);
                if (pe.hasIntervalTime && !pe.endTimeSet) {
                    canSend = false;
                    break;
                }
            }

            if(canSend) {
                Event[] eArray = mEvents.toArray(new Event[mEvents.size()]);
                saveTask = new TimerSaveTask(timerlogFile).execute(eArray);
                mEvents = new ArrayList<Event> ();
            } else {
                Log.e(t, "######### Queueing Timer Event");
            }


            Log.e(t, "######### Saving Timer Event");
        } else {
            Log.e(t, "######### Queueing Timer Event");
        }
    }

    /*
     * Use the time the survey was opened as a consistent value for wall clock time
     */
    private long getEventTime() {
        if(surveyOpenTime == 0) {
            surveyOpenTime = System.currentTimeMillis();
            surveyOpenElapsedTime = SystemClock.elapsedRealtime();
        }

        // debug
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateTime = sdf.format(surveyOpenTime + (SystemClock.elapsedRealtime() -  surveyOpenElapsedTime));
        Log.i(t, "%%%%%%%% " + currentDateTime);

        return surveyOpenTime + (SystemClock.elapsedRealtime() -  surveyOpenElapsedTime);
    }

}