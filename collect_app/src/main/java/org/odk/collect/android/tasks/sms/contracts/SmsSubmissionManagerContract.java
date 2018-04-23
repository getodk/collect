package org.odk.collect.android.tasks.sms.contracts;

import org.odk.collect.android.tasks.sms.SmsSubmissionModel;

public interface SmsSubmissionManagerContract {

    SmsSubmissionModel getSubmissionModelById(String instanceId);

    boolean markMessageAsSent(String instanceId,String messageId);

    void saveSubmissionListModel(SmsSubmissionModel model);
}



