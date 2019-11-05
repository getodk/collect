package org.odk.collect.android.formentry.audit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

public class AsyncTaskAuditEventWriter implements AuditEventLogger.AuditEventWriter {

    private static AsyncTask saveTask;

    @Override
    public void writeEvents(List<AuditEvent> auditEvents, @NonNull File file, boolean isLocationEnabled, boolean isTrackingChangesEnabled) {
        AuditEvent[] auditEventArray = auditEvents.toArray(new AuditEvent[0]);
        saveTask = new AuditEventSaveTask(file, isLocationEnabled, isTrackingChangesEnabled).execute(auditEventArray);
    }

    @Override
    public Boolean isWriting() {
        return saveTask != null && saveTask.getStatus() != AsyncTask.Status.FINISHED;
    }
}
