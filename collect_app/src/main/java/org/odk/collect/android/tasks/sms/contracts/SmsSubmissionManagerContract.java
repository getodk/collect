package org.odk.collect.android.tasks.sms.contracts;

import org.odk.collect.android.tasks.sms.SmsSubmissionModel;

public interface SmsSubmissionManagerContract {

    SmsSubmissionModel getSubmissionModelById(String instanceId);

    void saveSubmissionListModel(SmsSubmissionModel model);
}



