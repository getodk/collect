package org.odk.collect.android.sms.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.sms.SmsSubmissionManagerImpl;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Type;
import java.util.List;

import static org.odk.collect.android.tasks.sms.SmsSubmissionManagerImpl.KEY_SUBMISSION_LIST;

public abstract class BaseSmsTest {
    public static final String GATEWAY = "1918-344-4545";

    /**
     * Adds model to the Shared Preferences so that
     * Submission Manager has data to play with.
     */
    public void setupSmsSubmissionManagerData() {
        Context context;
        SharedPreferences.Editor editor;
        SharedPreferences sharedPreferences;

        context = RuntimeEnvironment.application;

        sharedPreferences = context.getSharedPreferences(SmsSubmissionManagerImpl.PREF_FILE_NAME, Context.MODE_PRIVATE);

        Type submissionModelListTpe = new TypeToken<List<SmsSubmission>>() {
        }.getType();

        String list = new Gson().toJson(SampleData.generateModels(), submissionModelListTpe);

        editor = sharedPreferences.edit();

        editor.putString(KEY_SUBMISSION_LIST, list);
        editor.commit();
    }

    public void setDefaultGateway() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        settings.edit().putString(PreferenceKeys.KEY_SMS_GATEWAY, GATEWAY).commit();
    }
}
