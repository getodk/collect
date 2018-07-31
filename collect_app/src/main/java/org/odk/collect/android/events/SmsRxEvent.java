package org.odk.collect.android.events;

import org.odk.collect.android.tasks.sms.models.SmsProgress;

import java.util.Date;

public class SmsRxEvent extends RxEvent {
    private SmsProgress progress;
    private String instanceId;
    private Date lastUpdated;
    private int resultCode;

    public SmsRxEvent() {
        progress = new SmsProgress();
    }

    public SmsRxEvent(String instanceId, int resultCode) {
        this.instanceId = instanceId;
        this.resultCode = resultCode;
        progress = new SmsProgress();
        lastUpdated = new Date();
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

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
}
