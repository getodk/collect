
package org.odk.collect.android.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.odk.collect.android.utilities.EventLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import timber.log.Timber;

/**
 * Background task for appending events to the event log
 */
public class EventSaveTask extends AsyncTask<EventLogger.Event, Void, Void> {
    private final @NonNull File file;
    private final boolean collectLocationCoordinates;

    private static final String CSV_HEADER = "event, node, start, end";
    private static final String CSV_HEADER_WITH_LOCATION_COORDINATES = CSV_HEADER + ", latitude, longitude, accuracy";

    public EventSaveTask(File file, boolean collectLocationCoordinates) {
        this.file = file;
        this.collectLocationCoordinates = collectLocationCoordinates;
    }

    @Override
    protected Void doInBackground(EventLogger.Event... params) {

        FileWriter fw = null;
        try {
            boolean newFile = !file.exists();
            fw = new FileWriter(file, true);
            if (newFile) {
                fw.write(getHeader());
            }
            if (params.length > 0) {
                for (EventLogger.Event ev : params) {
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
                    Timber.e("Attempt to close null FileWriter for EventLogger.");
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return null;
    }

    private String getHeader() {
        return collectLocationCoordinates
                ? CSV_HEADER_WITH_LOCATION_COORDINATES + "\n"
                : CSV_HEADER + "\n";
    }
}
