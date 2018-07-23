package org.odk.collect.android.tasks.sms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class SmsSubmissionManager implements SmsSubmissionManagerContract {
    private static SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public static final String PREF_FILE_NAME = "submissions_preferences";
    public static final String KEY_SUBMISSION = "submissions_list_key_";

    public SmsSubmissionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public SmsSubmission getSubmissionModel(String instanceId) {
        Type submissionModel = new TypeToken<SmsSubmission>() {
        }.getType();

        String submissionGson = preferences.getString(KEY_SUBMISSION + instanceId, "");

        SmsSubmission model;

        if (TextUtils.isEmpty(submissionGson)) {
            return null;
        }

        try {
            model = new Gson().fromJson(submissionGson, submissionModel);
        } catch (Exception e) {
            model = null;
        }

        return model;
    }

    @Override
    public boolean markMessageAsSent(String instanceId, int messageId) {

        SmsSubmission model = getSubmissionModel(instanceId);

        if (model == null) {
            return false;
        }

        List<Message> list = model.getMessages();

        boolean updated = false;
        for (Message message : list) {
            if (message.getId() == messageId) {
                message.setResultCode(Activity.RESULT_OK);
                list.set(list.indexOf(message), message);

                updated = true;
            }
        }

        model.setMessages(list);
        saveSubmission(model);

        return updated;
    }

    @Override
    public void markMessageAsSending(String instanceId, int messageId) {
        SmsSubmission model = getSubmissionModel(instanceId);

        if (model == null) {
            return;
        }

        List<Message> list = model.getMessages();

        for (Message message : list) {
            if (message.getId() == messageId) {
                message.setResultCode(SmsService.RESULT_SENDING);
                list.set(list.indexOf(message), message);
            }
        }

        model.setMessages(list);
        saveSubmission(model);
    }

    @Override
    public void forgetSubmission(String instanceId) {

        editor.remove(KEY_SUBMISSION + instanceId);
        editor.commit();
    }

    public void saveSubmission(SmsSubmission model) {

        Type submissionModel = new TypeToken<SmsSubmission>() {
        }.getType();

        String data = new Gson().toJson(model, submissionModel);
        editor.putString(KEY_SUBMISSION + model.getInstanceId(), data);
        editor.commit();
    }

    public void clearSubmissions() {
        preferences.edit().clear().apply();
    }

    public int checkNextMessageResultCode(String instanceId) {
        SmsSubmission model = getSubmissionModel(instanceId);

        Message message = model.getNextUnsentMessage();

        if (message == null) {
            return Activity.RESULT_OK;
        }

        return message.getResultCode();
    }

    @Override
    public void updateMessageStatus(int resultCode, String instanceId, int messageId) {
        SmsSubmission model = getSubmissionModel(instanceId);

        if (model == null) {
            return;
        }

        List<Message> list = model.getMessages();
        for (Message message : list) {
            if (message.getId() == messageId) {
                message.setResultCode(resultCode);
                list.set(list.indexOf(message), message);
            }
        }

        model.setMessages(list);
        model.setLastUpdated(new Date());
        saveSubmission(model);
    }
}