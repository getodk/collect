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

import java.io.File;
import java.util.ArrayList;

/**
 * Handle logging of timer events and pass them to an Async task to append to a file
 */
public class TimerLogger {

    public class Event {

        long start;
        int type;
        String node;
        long end;

        boolean hasIntervalTime;
        boolean endTimeSet;

        Event(long start, int type, String node, boolean hasIntervalTime) {
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
                case FormEntryController.EVENT_GROUP:
                    sType = "group questions";
                    break;
                case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                    sType = "prompt repeat";
                    break;
                case FormEntryController.EVENT_BEGINNING_OF_FORM:
                    sType = "start";
                    break;
                case FormEntryController.EVENT_END_OF_FORM:
                    sType = "end";
                    break;
                case -1:
                    sType = "finalize";     // TODO HACK
                    break;
                case -2:
                    sType = "survey start";     // TODO HACK
                    break;
                case -3:
                    sType = "survey resume";     // TODO HACK
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

    // Valid events
    public final static String OPEN = "open";
    public final static String BEGIN = "begin";
    public final static String END = "end";
    public final static String SAVE = "save";
    public final static String FINALIZE = "finalize";
    public final static String QUESTION_START = "qstart";
    public final static String QUESTION_END = "qend";

    public TimerLogger(File instancePath) {
        if(instancePath != null )
            timerlogFile =  new File(instancePath + File.separator + filename);
        mEvents = new ArrayList<Event>();
    }

    public void logTimerEvent(int type, TreeReference ref) {  // TODO customType

        // Calculate the time and add the event to the events array
        long start = System.currentTimeMillis();
        String node = ref == null ? "" : ref.toString();

        boolean hasIntervalTime = (type == FormEntryController.EVENT_QUESTION ? true : false);

        mEvents.add(new Event(start, type, node, hasIntervalTime));

        writeEvents();

    }

    public void setPath(String instancePath) {
        timerlogFile =  new File(instancePath + File.separator + filename);
    }

    public void exitView() {

        // Calculate the time and add the event to the events array
        long end = System.currentTimeMillis();
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

            }
            Event[] eArray = mEvents.toArray(new Event[mEvents.size()]);
            saveTask = new TimerSaveTask(timerlogFile).execute(eArray);
            mEvents = new ArrayList<Event> ();

            Log.e(t, "######### Saving Timer Event");
        } else {
            Log.e(t, "######### Queueing Timer Event");
        }
    }

}