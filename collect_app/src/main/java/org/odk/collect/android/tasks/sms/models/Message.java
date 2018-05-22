package org.odk.collect.android.tasks.sms.models;

import java.util.Random;

/**
 * Represents a single message that's been sent.
 */
public class Message {
    private int part;
    private int id;
    private String text;
    private MessageStatus messageStatus;

    public int getPart() {
        return part;
    }


    public int getId() {
        return id;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSent() {
        return messageStatus.equals(MessageStatus.Sent);
    }

    public boolean isSending() {
        return messageStatus.equals(MessageStatus.Sending);
    }

    public void generateRandomMessageID() {
        id = new Random().nextInt(10000);
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }
}
