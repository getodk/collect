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
import android.util.Log;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.tasks.TimerSaveTask;

import java.util.ArrayList;

/**
 * Handle logging of timer events and pass them to an Async task to append to a file
 */
public class TimerLogger {

    private class PendingEvent {

        long start;
        int type;
        String node;
        long end;

        boolean hasIntervalTime;
        boolean endTimeSet;

        public PendingEvent(long start, int type, String node, boolean hasIntervalTime) {
            this.start = start;
            this.type = type;
            this.node = node;
            this.hasIntervalTime = hasIntervalTime;
        }

        public void setEnd(long end) {
            this.end = end;
            this.endTimeSet = true;
        }

        public String toString() {
            String sType = "unknown";
            switch (type) {
                case FormEntryController.EVENT_QUESTION:
                    sType = "question";
                    break;
                case -1:
                    sType = "save";     // TODO HACK
                    break;
            }
            return sType + "," + node + "," + String.valueOf(start) + ","
                    + (hasIntervalTime ? String.valueOf(end) : "");
        }
    }

    private final static String t = "TimerLogger";
    private static AsyncTask saveTask = null;
    private ArrayList<PendingEvent> mPendingEvents = null;
    private String filename = "timer file";

    // Valid events
    public final static String OPEN = "open";
    public final static String BEGIN = "begin";
    public final static String END = "end";
    public final static String SAVE = "save";
    public final static String FINALIZE = "finalize";
    public final static String QUESTION_START = "qstart";
    public final static String QUESTION_END = "qend";

    public TimerLogger() {
        mPendingEvents = new ArrayList<PendingEvent>();
    }

    public void logTimerEvent(int type, TreeReference ref) {  // TODO customType

        // Calculate the time and add the event to the events array
        long start = System.currentTimeMillis();
        String node = ref == null ? "" : ref.toString();

        boolean hasIntervalTime = (type == FormEntryController.EVENT_QUESTION ? true : false);

        mPendingEvents.add(new PendingEvent(start, type, node, hasIntervalTime));

        writeEvents();

    }

    public void exitView() {

        // Calculate the time and add the event to the events array
        long end = System.currentTimeMillis();
        for (int i = 0; i < mPendingEvents.size(); i++) {
            mPendingEvents.get(i).setEnd(end);
        }
        writeEvents();
    }

    private void writeEvents() {
        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {

            int count = 0;
            for (int i = 0; count < mPendingEvents.size(); i++) {
                PendingEvent pe = mPendingEvents.get(i);
                if (!pe.hasIntervalTime || pe.endTimeSet) {
                    count++;
                } else {
                    break;      // Cannot save any more events until the final time has been set
                }
            }

            String[] eventArray = new String[count + 1];    // Add an entry for the filename
            eventArray[0] = filename;
            for (int i = 0; i < count; i++) {
                PendingEvent pe = mPendingEvents.get(0);
                eventArray[i + 1] = pe.toString();
                mPendingEvents.remove(0);
            }

            saveTask = new TimerSaveTask().execute(eventArray);

            Log.e(t, "######### Saving Timer Event");
        } else {
            Log.e(t, "######### Queueing Timer Event");
        }
    }

}