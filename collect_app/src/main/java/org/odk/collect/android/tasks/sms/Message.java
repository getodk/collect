package org.odk.collect.android.tasks.sms;

import java.util.Random;

public class Message {
    private int part;
    private int id;
    private String text;
    private boolean sent;
    private boolean sending;
    private String lastErrorMessage;

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
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public boolean isSending() {
        return sending;
    }

    public void setSending(boolean sending) {
        this.sending = sending;
    }

    public void generateRandomMessageID() {
        id = new Random().nextInt(10000);
    }
}
