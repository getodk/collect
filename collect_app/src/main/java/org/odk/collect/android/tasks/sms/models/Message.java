package org.odk.collect.android.tasks.sms.models;

import android.app.Activity;

import org.odk.collect.android.tasks.sms.SmsService;

import java.util.Random;

/**
 * Represents a message that could be all or partNumber of a submission but its status could be queued,failed etc.
 */
public class Message {
    private int partNumber;
    private int id;
    private String text;
    private int resultCode;

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
        return resultCode == Activity.RESULT_OK;
    }

    public boolean isSending() {
        return resultCode == SmsService.RESULT_SENDING;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public void generateRandomMessageID() {
        id = new Random().nextInt(Integer.MAX_VALUE);
    }
}
