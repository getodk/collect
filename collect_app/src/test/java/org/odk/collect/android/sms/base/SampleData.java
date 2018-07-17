package org.odk.collect.android.sms.base;

import android.app.Activity;
import android.telephony.SmsManager;

import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sample Data for SMS Tests
 */
public class SampleData {

    private SampleData() {
    }

    public static final String TEST_INSTANCE_ID = "test_instance";
    public static final String TEST_UNSENT_MESSAGE_INSTANCE_ID = "test_instance_unsent";

    public static SmsSubmission generateSampleModel() {

        SmsSubmission model = new SmsSubmission();

        model.setMessages(generateSampleMessages());
        model.setInstanceId(TEST_INSTANCE_ID);
        model.setLastUpdated(new Date());

        return model;
    }

    public static SmsSubmission generateUnsentSampleModel() {

        SmsSubmission model = new SmsSubmission();

        model.setMessages(generateUnsentSampleMessage());
        model.setInstanceId(TEST_UNSENT_MESSAGE_INSTANCE_ID);
        model.setLastUpdated(new Date());

        return model;
    }

    public static List<SmsSubmission> generateModels() {
        List<SmsSubmission> models = new ArrayList<>();
        models.add(generateSampleModel());
        models.add(generateUnsentSampleModel());

        return models;
    }

    public static List<Message> generateSampleMessages() {

        Message first = new Message();
        first.setResultCode(Activity.RESULT_OK);
        first.setPartNumber(1);
        first.generateRandomMessageID();
        first.setText("+N Joel Dean");

        Message second = new Message();
        second.setPartNumber(2);
        second.setResultCode(SmsService.RESULT_MESSAGE_READY);
        second.generateRandomMessageID();
        second.setText("+C America");

        Message third = new Message();
        third.setPartNumber(2);
        third.setResultCode(SmsService.RESULT_MESSAGE_READY);
        third.generateRandomMessageID();
        third.setText("+G Male");

        List<Message> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);

        return list;
    }

    public static List<Message> generateUnsentSampleMessage() {

        Message first = new Message();
        first.setResultCode(SmsManager.RESULT_ERROR_NULL_PDU);
        first.setPartNumber(1);
        first.generateRandomMessageID();
        first.setText("+N Joel Dean");

        Message second = new Message();
        second.setPartNumber(2);
        second.setResultCode(Activity.RESULT_OK);
        second.generateRandomMessageID();
        second.setText("+C America");

        Message third = new Message();
        third.setPartNumber(2);
        third.setResultCode(Activity.RESULT_OK);
        third.generateRandomMessageID();
        third.setText("+G Male");

        List<Message> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);

        return list;
    }
}
