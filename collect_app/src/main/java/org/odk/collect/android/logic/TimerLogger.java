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
package org.odk.collect.android.logic;

import android.os.AsyncTask;
import android.util.Log;

import org.odk.collect.android.tasks.TimerSaveTask;

/**
 * Handle logging of timer events and pass them to an Async task to append to a file
 */
public class TimerLogger  {
    private final static String t = "TimerLogger";
    private static AsyncTask saveTask = null;

    public TimerLogger() {

    }

    public void logTimerEvent() {

        if(saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {
            saveTask = new TimerSaveTask().execute();
            Log.e(t, "######### Saving Timer Event");
        } else {
            Log.e(t, "######### Queueing Timer Event");
        }
    }

}