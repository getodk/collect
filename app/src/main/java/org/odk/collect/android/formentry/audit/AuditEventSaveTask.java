
package org.odk.collect.android.formentry.audit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import timber.log.Timber;

import static org.odk.collect.android.formentry.audit.AuditEventCSVLine.toCSVLine;

/**
 * Background task for appending events to the event log
 */
public class AuditEventSaveTask extends AsyncTask<AuditEvent, Void, Void> {
    private final @NonNull
    File file;
    private final boolean isLocationEnabled;
    private final boolean isTrackingChangesEnabled;
    private final boolean isUserRequired;
    private final boolean isTrackChangesReasonEnabled;

    private static final String DEFAULT_COLUMNS = "event,node,start,end";
    private static final String LOCATION_COORDINATES_COLUMNS = ",latitude,longitude,accuracy";
    private static final String ANSWER_VALUES_COLUMNS = ",old-value,new-value";
    private static final String USER_COLUMNS = ",user";
    private static final String CHANGE_REASON_COLUMNS = ",change-reason";

    public AuditEventSaveTask(@NonNull File file, boolean isLocationEnabled, boolean isTrackingChangesEnabled, boolean isUserRequired, boolean isTrackChangesReasonEnabled) {
        this.file = file;
        this.isLocationEnabled = isLocationEnabled;
        this.isTrackingChangesEnabled = isTrackingChangesEnabled;
        this.isUserRequired = isUserRequired;
        this.isTrackChangesReasonEnabled = isTrackChangesReasonEnabled;
    }

    @Override
    protected Void doInBackground(AuditEvent... params) {
        FileWriter fw = null;
        try {
            boolean newFile = !file.exists();
            fw = new FileWriter(file, true);
            if (newFile) {
                fw.write(getHeader() + "\n");
            } else if (updateHeaderIfNeeded()) {
                fw.close();
                fw = new FileWriter(file.getAbsolutePath(), true);
            }
            if (params.length > 0) {
                for (AuditEvent aev : params) {
                    String csvLine = toCSVLine(aev, isLocationEnabled, isTrackingChangesEnabled, isTrackChangesReasonEnabled);
                    fw.write(csvLine + "\n");
                    Timber.i("Log audit event: %s", csvLine);
                }
            }
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                } else {
                    Timber.e(new Error("Attempt to close null FileWriter for AuditEventLogger."));
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
            if (shouldHeaderBeUpdated(br.readLine())) { // update header
                File temporaryFile = new File(file.getParentFile().getAbsolutePath() + "/temporaryAudit.csv");
                tfw = new FileWriter(temporaryFile, true);
                tfw.write(getHeader() + "\n");
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
                    Timber.e(new Error("Attempt to close null FileWriter for AuditEventLogger."));
                }
                if (br != null) {
                    br.close();
                } else {
                    Timber.e(new Error("Attempt to close null BufferedReader for AuditEventLogger."));
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return headerUpdated;
    }

    private boolean shouldHeaderBeUpdated(String header) {
        return header == null
                || (isLocationEnabled && !header.contains(LOCATION_COORDINATES_COLUMNS))
                || (isTrackingChangesEnabled && !header.contains(ANSWER_VALUES_COLUMNS))
                || (isUserRequired && !header.contains(USER_COLUMNS));
    }

    private String getHeader() {
        String header = DEFAULT_COLUMNS;
        if (isLocationEnabled) {
            header += LOCATION_COORDINATES_COLUMNS;
        }
        if (isTrackingChangesEnabled) {
            header += ANSWER_VALUES_COLUMNS;
        }
        if (isUserRequired) {
            header += USER_COLUMNS;
        }
        if (isTrackChangesReasonEnabled) {
            header += CHANGE_REASON_COLUMNS;
        }
        return header;
    }
}
