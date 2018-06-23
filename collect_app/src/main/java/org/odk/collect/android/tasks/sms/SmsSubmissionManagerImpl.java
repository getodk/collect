package org.odk.collect.android.tasks.sms;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class SmsSubmissionManagerImpl implements SmsSubmissionManagerContract {
    private static SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public static final String PREF_FILE_NAME = "submissions_preferences";
    public static final String KEY_SUBMISSION = "submissions_list_key";

    public SmsSubmissionManagerImpl(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public SmsSubmission getSubmissionModel(String instanceId) {
        return getSubmissionFromPrefs(instanceId);
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
                message.setMessageStatus(MessageStatus.Sent);
                list.set(list.indexOf(message), message);

                updated = true;
            }
        }

        model.setMessages(list);
        saveSubmission(model);

        return updated;
    }

    @Override
    public boolean markMessageAsSending(String instanceId, int messageId) {
        SmsSubmission model = getSubmissionModel(instanceId);

        if (model == null) {
            return false;
        }

        List<Message> list = model.getMessages();

        boolean updated = false;
        for (Message message : list) {
            if (message.getId() == messageId) {
                message.setMessageStatus(MessageStatus.Sending);
                list.set(list.indexOf(message), message);

                updated = true;
            }
        }

        model.setMessages(list);
        saveSubmission(model);

        return updated;
    }

    @Override
    public boolean updateMessageStatus(MessageStatus messageStatus, String instanceId, int messageId) {
        SmsSubmission model = getSubmissionModel(instanceId);

        if (model == null) {
            return false;
        }

        List<Message> list = model.getMessages();

        boolean updated = false;
        for (Message message : list) {
            if (message.getId() == messageId) {
                message.setMessageStatus(messageStatus);
                list.set(list.indexOf(message), message);

                updated = true;
            }
        }

        model.setMessages(list);
        model.setLastUpdated(new Date());
        saveSubmission(model);

        return updated;
    }

    @Override
    public void deleteSubmission(String instanceId) {

        editor.remove(KEY_SUBMISSION + "_" + instanceId);
        editor.commit();
    }

    public void saveSubmission(SmsSubmission model) {

        Type submissionModel = new TypeToken<SmsSubmission>() {
        }.getType();

        String data = new Gson().toJson(model, submissionModel);
        editor.putString(KEY_SUBMISSION + "_" + model.getInstanceId(), data);
        editor.commit();
    }

    @Override
    public void clearSubmissions() {
        preferences.edit().clear().apply();
    }

    private SmsSubmission getSubmissionFromPrefs(String instanceId) {
        Type submissionModel = new TypeToken<SmsSubmission>() {
        }.getType();

        String list = preferences.getString(KEY_SUBMISSION + "_" + instanceId, "");

        SmsSubmission model;

        if (TextUtils.isEmpty(list)) {
            return null;
        }

        try {
            model = new Gson().fromJson(list, submissionModel);
        } catch (Exception e) {
            model = null;
        }

        return model;
    }

    public MessageStatus checkNextMessageStatus(String instanceId) {
        SmsSubmission model = getSubmissionModel(instanceId);

        Message message = model.getNextUnsentMessage();

        if (message == null) {
            return null;
        }

        return message.getMessageStatus();
    }
}