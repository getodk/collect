package org.odk.collect.android.events;

import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SmsSubmissionProgress;

public class SmsProgressEvent {
    private MessageStatus status;
    private SmsSubmissionProgress progress;
    private String instanceId;

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public SmsSubmissionProgress getProgress() {
        return progress;
    }

    public void setProgress(SmsSubmissionProgress progress) {
        this.progress = progress;
    }
}
