
package org.odk.collect.android.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.odk.collect.android.logic.Event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import timber.log.Timber;

/**
 * Background task for appending events to the event log
 */
public class EventSaveTask extends AsyncTask<Event, Void, Void> {
    private final @NonNull File file;
    private final boolean isLocationEnabled;

    private static final String CSV_HEADER = "event, node, start, end";
    private static final String CSV_HEADER_WITH_LOCATION_COORDINATES = CSV_HEADER + ", latitude, longitude, accuracy";

    public EventSaveTask(File file, boolean isLocationEnabled) {
        this.file = file;
        this.isLocationEnabled = isLocationEnabled;
    }

    @Override
    protected Void doInBackground(Event... params) {

        FileWriter fw = null;
        try {
            boolean newFile = !file.exists();
            fw = new FileWriter(file, true);
            if (newFile) {
                fw.write(getHeader());
            } else if (isLocationEnabled) {
                if (updateHeaderIfNeeded()) {
                    fw.close();
                    fw = new FileWriter(file.getAbsolutePath(), true);
                }
            }
            if (params.length > 0) {
                for (Event ev : params) {
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

    private boolean updateHeaderIfNeeded() {
        boolean headerUpdated = false;
        FileWriter tfw = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String header = br.readLine();
            if (header != null && header.equals(CSV_HEADER)) { // update header
                File temporaryFile = new File(file.getParentFile().getAbsolutePath() + "/temporaryAudit.csv");
                tfw = new FileWriter(temporaryFile, true);
                tfw.write(CSV_HEADER_WITH_LOCATION_COORDINATES + "\n");
                String line;
                while ((line = br.readLine()) != null) {
                    tfw.write(line + "\n");
                }
                temporaryFile.renameTo(file);
                headerUpdated = true;
            }
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            try {
                if (tfw != null) {
                    tfw.close();
                } else {
                    Timber.e("Attempt to close null FileWriter for EventLogger.");
                }
                if (br != null) {
                    br.close();
                } else {
                    Timber.e("Attempt to close null BufferedReader for EventLogger.");
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return headerUpdated;
    }

    private String getHeader() {
        return isLocationEnabled
                ? CSV_HEADER_WITH_LOCATION_COORDINATES + "\n"
                : CSV_HEADER + "\n";
    }
}
