package org.odk.collect.android.events;

import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SmsProgress;

import java.util.Date;

public class SmsEvent extends RxEvent {
    private MessageStatus status;
    private SmsProgress progress;
    private String instanceId;
    private Date lastUpdated;

    public SmsEvent() {
        progress = new SmsProgress();
    }

    public SmsEvent(String instanceId, MessageStatus status) {
        this.status = status;
        this.instanceId = instanceId;
        progress = new SmsProgress();
    }

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

    public SmsProgress getProgress() {
        return progress;
    }

    public void setProgress(SmsProgress progress) {
        this.progress = progress;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
