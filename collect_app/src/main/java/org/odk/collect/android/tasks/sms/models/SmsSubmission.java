package org.odk.collect.android.tasks.sms.models;

import java.util.Date;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;

/**
 * Tracks an instance's sms submission.
 */
public class SmsSubmission {
    private String instanceId;
    private int notificationId;
    private List<Message> messages;
    private Date lastUpdated;
    private int jobId;
    private String displayName;

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Returns the next message to be processed. If there's no other message
     * that simply means all messages have been sent.
     *
     * @return next message to be sent.
     */
    public Message getNextUnsentMessage() {
        return Observable.fromIterable(messages)
                .filter(message -> !message.isSent())
                .sorted((message, otherMessage) -> message.getPart() - otherMessage.getPart())
                .firstElement()
                .blockingGet();
    }

    public SmsProgress getCompletion() {
        SmsProgress progress = new SmsProgress();

        long complete = Observable.fromIterable(messages)
                .filter(Message::isSent)
                .count().blockingGet();

        int total = messages.size();

        progress.setCompletedCount((int) complete);
        progress.setTotalCount(total);

        return progress;
    }

    /***
     * Checks to see if the current message is the last or not. If it's not the last it sends
     * Sending as the status to the InstanceUploaderList so that it shows the current progress but if it's the last
     * message then that means the submission has been completed so it should show Sent. This has to be done
     * because the MessageStatus is being used to serve SmsSender Layer and it's status is also tied to the UI.
     *
     * @param status that was received from the broadcast receiver of the message that was just sent.
     * @return the MessageStatus that will be transferred via the event.
     */
    public MessageStatus isSentOrSending(MessageStatus status) {
        if (status.equals(MessageStatus.Sent)) {
            if (isSubmissionComplete()) {
                return MessageStatus.Sent;
            } else {
                return MessageStatus.Sending;
            }
        }

        return status;
    }


    public int getJobId() {
        return jobId;
    }

    public boolean isSubmissionComplete() {
        for (Message message : messages) {
            if (!message.isSent()) {
                return false;
            }
        }

        return true;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId() {
        this.notificationId = new Random().nextInt(10000);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
