package org.odk.collect.android.tasks.sms.models;

/**
 * Encapsulates all the data that's received when the SentBroadcastReceiver
 * is triggered. This is then passed to the SMSService for processing.
 */
public class SentMessageResult {
    private MessageResultStatus messageResultStatus;
    private int messageId;
    private String instanceId;

    public MessageResultStatus getMessageResultStatus() {
        return messageResultStatus;
    }

    public void setMessageResultStatus(MessageResultStatus messageResultStatus) {
        this.messageResultStatus = messageResultStatus;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
