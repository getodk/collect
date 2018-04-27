package org.odk.collect.android.tasks.sms.models;

import java.io.Serializable;

/**
 * A model that represents the data that a SMSSenderJob
 * needs to perform it's task.
 */
public class SmsJobMessage implements Serializable {
    private int messageId;
    private String text;
    private String gateway;
    private String instanceId;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
