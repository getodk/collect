package org.odk.collect.android.tasks.sms.contracts;

import org.odk.collect.android.tasks.sms.SmsSubmissionModel;

public interface SmsSubmissionManagerContract {

    SmsSubmissionModel getSubmissionModel(String instanceId);

    boolean markMessageAsSent(String instanceId,int messageId);

    boolean markMessageAsSending(String instanceId, int messageId);

    void deleteSubmission(String instanceId);

    void saveSubmission(SmsSubmissionModel model);

    void clearSubmissions();
}



