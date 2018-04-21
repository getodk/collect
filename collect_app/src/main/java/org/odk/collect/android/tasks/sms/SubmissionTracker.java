package org.odk.collect.android.tasks.sms;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubmissionTracker{
    private static SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private static final String PREF_FILE_NAME = "submissions_preferences";
    private static final String KEY_SUBMISSION_LIST = "submissions_list";

    public SubmissionTracker(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        editor = this.preferences.edit();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    public SmsSubmissionModel getSubmissionModelById(String instanceId)
    {
        List<SmsSubmissionModel> models = getSubmissionListFromPrefs();

        for(SmsSubmissionModel model:models)
        {
            if(model.getInstanceId().equals(instanceId))
                return model;
        }

        return null;
    }

    private void saveSubmissionListModel(SmsSubmissionModel model) {

        Type submissionModelListTpe = new TypeToken<List<SmsSubmissionModel>>() {
        }.getType();

        List<SmsSubmissionModel> models = getSubmissionListFromPrefs();

        model.setDateAdded(new Date());

        models.add(model);

        String list = new Gson().toJson(models, submissionModelListTpe);
        editor.putString(KEY_SUBMISSION_LIST, list);
        editor.commit();
    }

    private List<SmsSubmissionModel> getSubmissionListFromPrefs() {
        Type submissionModelListTpe = new TypeToken<List<SmsSubmissionModel>>() {
        }.getType();
        String list = preferences.getString(KEY_SUBMISSION_LIST, "");

        List<SmsSubmissionModel> models = new ArrayList<>();

        if (TextUtils.isEmpty(list))
            return models;

        try {
            models = new Gson().fromJson(list, submissionModelListTpe);
        }catch (Exception e)
        {
            models = new ArrayList<>();
        }

        return models;

    }
}