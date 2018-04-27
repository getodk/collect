package org.odk.collect.android.sms.base;

import org.odk.collect.android.tasks.sms.Message;
import org.odk.collect.android.tasks.sms.SmsSubmissionModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sample Data for SMS Tests
 */
public class SampleData {

    public static String TEST_INSTANCE_ID = "test_instance";
    public static SmsSubmissionModel generateSampleModel() {

        SmsSubmissionModel model = new SmsSubmissionModel();

        model.setMessages(generateSampleMessages());
        model.setInstanceId(TEST_INSTANCE_ID);
        model.setDateAdded(new Date());

        return model;
    }

    public static List<SmsSubmissionModel> generateModels(){
        List<SmsSubmissionModel> models = new ArrayList<>();
        models.add(generateSampleModel());

        return models;
    }

    public static List<Message> generateSampleMessages() {

        Message first = new Message();
        first.setSent(true);
        first.setPart(1);
        first.generateRandomMessageID();
        first.setText("+N Joel Dean");
        first.setSending(false);

        Message second = new Message();
        second.setSent(false);
        second.setPart(2);
        second.generateRandomMessageID();
        second.setText("+C America");
        second.setSending(false);

        Message third = new Message();
        third.setSent(false);
        third.setPart(2);
        third.generateRandomMessageID();
        third.setText("+G Male");
        third.setSending(false);

        List<Message> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);

        return list;
    }
}
