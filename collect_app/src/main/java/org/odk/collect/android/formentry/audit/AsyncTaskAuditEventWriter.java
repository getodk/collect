package org.odk.collect.android.formentry.audit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

public class AsyncTaskAuditEventWriter implements AuditEventLogger.AuditEventWriter {

    private static AsyncTask saveTask;
    private final File file;
    private final boolean isLocationEnabled;
    private final boolean isTrackingChangesEnabled;
    private final boolean isUserRequired;
    private final boolean isTrackChangesReasonEnabled;

    public AsyncTaskAuditEventWriter(@NonNull File file, boolean isLocationEnabled, boolean isTrackingChangesEnabled, boolean isUserRequired, boolean isTrackChangesReasonEnabled) {
        this.file = file;
        this.isLocationEnabled = isLocationEnabled;
        this.isTrackingChangesEnabled = isTrackingChangesEnabled;
        this.isUserRequired = isUserRequired;
        this.isTrackChangesReasonEnabled = isTrackChangesReasonEnabled;
    }

    @Override
    public void writeEvents(List<AuditEvent> auditEvents) {
        AuditEvent[] auditEventArray = auditEvents.toArray(new AuditEvent[0]);
        saveTask = new AuditEventSaveTask(file, isLocationEnabled, isTrackingChangesEnabled, isUserRequired, isTrackChangesReasonEnabled).execute(auditEventArray);
    }

    @Override
    public boolean isWriting() {
        return saveTask != null && saveTask.getStatus() != AsyncTask.Status.FINISHED;
    }
}
