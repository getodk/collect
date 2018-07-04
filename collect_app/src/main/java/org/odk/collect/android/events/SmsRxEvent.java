package org.odk.collect.android.events;

import org.odk.collect.android.tasks.sms.models.SmsStatus;
import org.odk.collect.android.tasks.sms.models.SmsProgress;

import java.util.Date;

public class SmsRxEvent extends RxEvent {
    private SmsStatus status;
    private SmsProgress progress;
    private String instanceId;
    private Date lastUpdated;

    public SmsRxEvent() {
        progress = new SmsProgress();
    }

    public SmsRxEvent(String instanceId, SmsStatus status) {
        this.status = status;
        this.instanceId = instanceId;
        progress = new SmsProgress();
    }

    public SmsStatus getStatus() {
        return status;
    }

    public void setStatus(SmsStatus status) {
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
