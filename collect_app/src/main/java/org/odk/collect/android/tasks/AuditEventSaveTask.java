
package org.odk.collect.android.tasks;

import android.os.AsyncTask;
import androidx.annotation.NonNull;

import org.odk.collect.android.logic.AuditEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import timber.log.Timber;

/**
 * Background task for appending events to the event log
 */
public class AuditEventSaveTask extends AsyncTask<AuditEvent, Void, Void> {
    private final @NonNull File file;
    private final boolean isLocationEnabled;
    private final boolean isTrackingChangesEnabled;
    private final String expectedHeader;

    private static final String DEFAULT_COLUMNS = "event, node, start, end";
    private static final String LOCATION_COORDINATES_COLUMNS = ", latitude, longitude, accuracy";
    private static final String ANSWER_VALUES_COLUMNS = ", old-value, new-value";

    public AuditEventSaveTask(@NonNull File file, boolean isLocationEnabled, boolean isTrackingChangesEnabled) {
        this.file = file;
        this.isLocationEnabled = isLocationEnabled;
        this.isTrackingChangesEnabled = isTrackingChangesEnabled;
        expectedHeader = getHeader();
    }

    @Override
    protected Void doInBackground(AuditEvent... params) {
        FileWriter fw = null;
        try {
            boolean editedFile = file.exists();
            fw = new FileWriter(file, true);
            if (shouldHeaderBeAdded()) {
                if (editedFile) {
                    fw.write("\n");
                }
                fw.write(expectedHeader + "\n");
            }
            for (AuditEvent aev : params) {
                fw.write(aev.toString() + "\n");
                Timber.i("Log audit event: %s", aev.toString());
            }
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                } else {
                    Timber.e("Attempt to close null FileWriter for AuditEventLogger.");
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return null;
    }

    private boolean shouldHeaderBeAdded() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String currentHeader = null;
            String line;
            while ((line = br.readLine()) != null)   {
                if (line.startsWith(DEFAULT_COLUMNS)) {
                    currentHeader = line;
                }
            }

            return !expectedHeader.equals(currentHeader);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                } else {
                    Timber.e("Attempt to close null BufferedReader for AuditEventLogger.");
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return false;
    }

    private String getHeader() {
        String header = DEFAULT_COLUMNS;
        if (isLocationEnabled) {
            header += LOCATION_COORDINATES_COLUMNS;
        }
        if (isTrackingChangesEnabled) {
            header += ANSWER_VALUES_COLUMNS;
        }
        return header;
    }
}
