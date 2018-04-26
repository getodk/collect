package org.odk.collect.android.sms;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.tasks.sms.SmsSubmissionManagerImpl;
import org.odk.collect.android.tasks.sms.SmsSubmissionModel;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Type;
import java.util.List;

import static org.odk.collect.android.tasks.sms.SmsSubmissionManagerImpl.KEY_SUBMISSION_LIST;

public abstract class BaseSmsTest {

    public void setupSmsSubmissionManagerData() {
        Context context;
        SharedPreferences.Editor editor;
        SharedPreferences sharedPreferences;

        context = RuntimeEnvironment.application;

        sharedPreferences = context.getSharedPreferences(SmsSubmissionManagerImpl.PREF_FILE_NAME, Context.MODE_PRIVATE);

        Type submissionModelListTpe = new TypeToken<List<SmsSubmissionModel>>() {
        }.getType();

        String list = new Gson().toJson(SampleData.generateModels(), submissionModelListTpe);

        editor = sharedPreferences.edit();

        editor.putString(KEY_SUBMISSION_LIST, list);
        editor.commit();
    }

}
