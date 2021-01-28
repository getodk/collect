package org.odk.collect.android.formentry.audit;

import java.util.ArrayList;
import java.util.List;

public class TestWriter implements AuditEventLogger.AuditEventWriter {

    List<AuditEvent> auditEvents = new ArrayList<>();

    @Override
    public void writeEvents(List<AuditEvent> auditEvents) {
        this.auditEvents.addAll(auditEvents);
    }

    @Override
    public boolean isWriting() {
        return false;
    }
}