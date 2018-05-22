package org.odk.collect.android.tasks.sms.models;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * Tracks an instance's sms submission.
 */
public class SmsSubmission {
    private String instanceId;
    private List<Message> messages;
    private Date lastUpdated;
    private int jobId;

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
                .filter(message -> message.isSent())
                .count().blockingGet();

        int total = messages.size();

        progress.setCompletedCount((int) complete);
        progress.setTotalCount(total);

        return progress;
    }

    public void saveMessage(Message message) {
        int index = getMessages().indexOf(message);
        getMessages().set(index, message);
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
}
