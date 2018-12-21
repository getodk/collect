
package org.odk.collect.android.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.odk.collect.android.utilities.TimerLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import timber.log.Timber;

/**
 * Background task for appending a timer event to the timer log
 */
public class TimerSaveTask extends AsyncTask<TimerLogger.Event, Void, Void> {
    private final @NonNull File file;
    private final boolean collectLocationCoordinates;

    private static final String TIMING_CSV_HEADER = "event, node, start, end";
    private static final String TIMING_CSV_HEADER_WITH_LOCATION_COORDINATES = TIMING_CSV_HEADER + ", latitude, longitude, accuracy";

    public TimerSaveTask(File file, boolean collectLocationCoordinates) {
        this.file = file;
        this.collectLocationCoordinates = collectLocationCoordinates;
    }

    @Override
    protected Void doInBackground(TimerLogger.Event... params) {

        FileWriter fw = null;
        try {
            boolean newFile = !file.exists();
            fw = new FileWriter(file, true);
            if (newFile) {
                fw.write(getHeader());
            }
            if (params.length > 0) {
                for (TimerLogger.Event ev : params) {
                    fw.write(ev.toString() + "\n");
                    Timber.i("Log audit Event: %s", ev.toString());
                }
            }
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                } else {
                    Timber.e("Attempt to close null FileWriter for TimerLogger.");
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return null;
    }

    private String getHeader() {
        return collectLocationCoordinates
                ? TIMING_CSV_HEADER_WITH_LOCATION_COORDINATES + "\n"
                : TIMING_CSV_HEADER + "\n";
    }
}
