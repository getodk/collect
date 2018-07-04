package org.odk.collect.android.tasks.sms.models;

import java.util.Random;

/**
 *  Represents a message that could be all or partNumber of a submission but its status could be queued,failed etc.
 */
public class Message {
    private int partNumber;
    private int id;
    private String text;
    private SmsStatus smsStatus;

    public int getPartNumber() {
        return partNumber;
    }

    public int getId() {
        return id;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSent() {
        return smsStatus.equals(SmsStatus.Sent);
    }

    public boolean isSending() {
        return smsStatus.equals(SmsStatus.Sending);
    }

    public void generateRandomMessageID() {
        id = new Random().nextInt(10000);
    }

    public SmsStatus getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(SmsStatus smsStatus) {
        this.smsStatus = smsStatus;
    }
}
