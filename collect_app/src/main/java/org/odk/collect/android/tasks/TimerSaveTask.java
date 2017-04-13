
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
    private static String TIMING_CSV_HEADER = "event, node, start, end, dirn";

    public TimerSaveTask(File file) {
        this.file = file;
    }

    @Override
    protected Void doInBackground(TimerLogger.Event... params) {

        FileWriter fw = null;
        try {
            boolean newFile = !file.exists();
            fw = new FileWriter(file, true);
            if (newFile) {
                fw.write(TIMING_CSV_HEADER + "\n");
            }
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
