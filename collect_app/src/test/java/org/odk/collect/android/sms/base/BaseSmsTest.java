package org.odk.collect.android.sms.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.sms.SmsSubmissionManager;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Type;

import static org.odk.collect.android.tasks.sms.SmsSubmissionManager.KEY_SUBMISSION;

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

        sharedPreferences = context.getSharedPreferences(SmsSubmissionManager.PREF_FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Type submissionModelListTpe = new TypeToken<SmsSubmission>() {
        }.getType();

        for (SmsSubmission smsSubmission : SampleData.generateModels()) {
            String data = new Gson().toJson(smsSubmission, submissionModelListTpe);

            editor.putString(KEY_SUBMISSION + smsSubmission.getInstanceId(), data);
            editor.commit();
        }
    }

    public void setDefaultGateway() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        settings.edit().putString(PreferenceKeys.KEY_SMS_GATEWAY, GATEWAY).commit();
    }
}
