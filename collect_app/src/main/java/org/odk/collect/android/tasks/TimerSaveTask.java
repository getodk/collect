/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.odk.collect.android.utilities.TimerLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.os.SystemClock.sleep;

/**
 * Background task for appending a timer event to the timer log
 */
public class TimerSaveTask extends AsyncTask<TimerLogger.Event, Void, Void> {
    private final static String t = "TimerSaveTask";
    private static File file;

    public TimerSaveTask(File file) {
        this.file = file;
    }

    @Override
    protected Void doInBackground(TimerLogger.Event... params) {

        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true);
            if (params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    fw.write(params[i].toString() + "\n");
                }
            }
        } catch (IOException e) {
            Log.e(t, "error writing timer log", e);
        } finally {
            try {
                fw.close();
            } catch (Exception e) {
            }
            ;
        }
        return null;
    }


}
