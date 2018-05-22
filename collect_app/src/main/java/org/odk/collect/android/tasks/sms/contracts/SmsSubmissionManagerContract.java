package org.odk.collect.android.tasks.sms.contracts;

import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

/**
 * Contract for a component that's utilized to track sms submissions.
 */
public interface SmsSubmissionManagerContract {

    SmsSubmission getSubmissionModel(String instanceId);

    boolean markMessageAsSent(String instanceId, int messageId);

    boolean markMessageAsSending(String instanceId, int messageId);

    void deleteSubmission(String instanceId);

    void saveSubmission(SmsSubmission model);

    void clearSubmissions();

    MessageStatus checkNextMessageStatus(String instanceId);

    boolean updateMessageStatus(MessageStatus messageStatus, String instanceId, int messageId);
}



