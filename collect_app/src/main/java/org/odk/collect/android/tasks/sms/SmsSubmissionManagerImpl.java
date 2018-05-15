package org.odk.collect.android.tasks.sms;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SmsSubmissionManagerImpl implements SmsSubmissionManagerContract {
    private static SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final String PREF_FILE_NAME = "submissions_preferences";
    public static final String KEY_SUBMISSION_LIST = "submissions_list";

    public SmsSubmissionManagerImpl(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        editor = this.preferences.edit();
    }


    public SmsSubmission getSubmissionModel(String instanceId) {
        List<SmsSubmission> models = getSubmissionListFromPrefs();

        for (SmsSubmission model : models) {
            if (model.getInstanceId().equals(instanceId)) {
                return model;
            }
        }

        return null;
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
                message.setSent(true);
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
                message.setSending(true);
                list.set(list.indexOf(message), message);

                updated = true;
            }
        }

        model.setMessages(list);
        saveSubmission(model);

        return updated;
    }

    @Override
    public void deleteSubmission(String instanceId) {
        List<SmsSubmission> models = getSubmissionListFromPrefs();

        SmsSubmission model = null;

        for (SmsSubmission submissionModel : models) {
            if (submissionModel.getInstanceId().equals(instanceId)) {
                model = submissionModel;
            }
        }

        if (model != null) {
            models.remove(model);
            saveSubmissionListToPrefs(models);
        }
    }

    public void saveSubmission(SmsSubmission model) {

        Type submissionModelListTpe = new TypeToken<List<SmsSubmission>>() {
        }.getType();

        List<SmsSubmission> models = getSubmissionListFromPrefs();

        boolean previousModelFound = false;

        for (SmsSubmission smsSubmission : models) {
            if (smsSubmission.getInstanceId().equals(model.getInstanceId())) {

                models.set(models.indexOf(smsSubmission), model);

                previousModelFound = true;
            }
        }

        if (!previousModelFound) {
            models.add(model);
        }

        String list = new Gson().toJson(models, submissionModelListTpe);
        editor.putString(KEY_SUBMISSION_LIST, list);
        editor.commit();
    }

    @Override
    public void clearSubmissions() {
        preferences.edit().clear().apply();
    }

    private void saveSubmissionListToPrefs(List<SmsSubmission> models) {

        Type submissionModelListTpe = new TypeToken<List<SmsSubmission>>() {
        }.getType();

        String list = new Gson().toJson(models, submissionModelListTpe);
        editor.putString(KEY_SUBMISSION_LIST, list);
        editor.commit();
    }


    private List<SmsSubmission> getSubmissionListFromPrefs() {
        Type submissionModelListTpe = new TypeToken<List<SmsSubmission>>() {
        }.getType();
        String list = preferences.getString(KEY_SUBMISSION_LIST, "");

        List<SmsSubmission> models = new ArrayList<>();

        if (TextUtils.isEmpty(list)) {
            return models;
        }

        try {
            models = new Gson().fromJson(list, submissionModelListTpe);
        } catch (Exception e) {
            models = new ArrayList<>();
        }

        return models;
    }
}