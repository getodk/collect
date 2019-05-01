package org.odk.collect.android;

import android.app.Activity;

import androidx.test.rule.ActivityTestRule;
import android.text.format.Formatter;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.SmsSubmissionManager;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.test.FormLoadingUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.odk.collect.android.test.FormLoadingUtils.ALL_WIDGETS_FORM;

/**
 * Generates submissions and then checks to see if the device runs out of memory
 * when they are being persisted to SharedPreferences
 */
public class SmsSubmissionMemoryTest {
    @Rule
    public ActivityTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(ALL_WIDGETS_FORM);

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final float LOW_MEMORY_THRESHOLD_PERCENT = 10f;
    private static final int SUBMISSION_POOL_SIZE = 1000;
    private static final double STATUS_REPORT_PERCENT = .15 * SUBMISSION_POOL_SIZE;

    @Ignore("SMS is currently disabled and this takes a while to run")
    @Test
    public void testMemoryConsumption() {
        SmsSubmissionManager manager = new SmsSubmissionManager(activityTestRule.getActivity());

        manager.clearSubmissions();

        List<SmsSubmission> models = generateModels(SUBMISSION_POOL_SIZE);

        String startingMessage = "Starting memory : " + getCurrentMemory();
        activityTestRule.getActivity().runOnUiThread(() -> ToastUtils.showLongToast(startingMessage));
        Timber.i(startingMessage);

        for (SmsSubmission model : models) {
            manager.saveSubmission(model);

            int index = models.indexOf(model);
            if (index % STATUS_REPORT_PERCENT == 0 && index > 0) {
                String message;

                if (isMemoryLow()) {
                    message = "At submission " + index + " the app ran out of memory.Consumption is at " + getCurrentMemory();
                    throw new OutOfMemoryError(message);
                } else {
                    message = "At submission " + index + " memory consumption is at " + getCurrentMemory();
                }

                activityTestRule.getActivity().runOnUiThread(() -> ToastUtils.showLongToast(message));
                Timber.i(message);
            }
        }

        assertFalse(isMemoryLow());
        assertNotNull(manager.getSubmissionModel(String.valueOf(SUBMISSION_POOL_SIZE)));
    }

    private String getCurrentMemory() {
        Runtime runtime = Runtime.getRuntime();
        long memorySize = runtime.totalMemory() - runtime.freeMemory();
        return Formatter.formatShortFileSize(activityTestRule.getActivity(), memorySize);
    }

    boolean isMemoryLow() {
        // Get app memory info
        long available = Runtime.getRuntime().maxMemory();
        long used = Runtime.getRuntime().totalMemory();

        // Check for and handle low memory state
        float percentAvailable = 100f * (1f - ((float) used / available));
        return percentAvailable <= LOW_MEMORY_THRESHOLD_PERCENT;
    }

    private SmsSubmission generateSampleModel(int instanceId) {
        SmsSubmission model = new SmsSubmission();

        model.setMessages(generateSampleMessages());
        model.setInstanceId(String.valueOf(instanceId));
        model.setLastUpdated(new Date());

        return model;
    }

    List<SmsSubmission> generateModels(int amount) {
        List<SmsSubmission> models = new ArrayList<>();

        for (int x = 0; x <= amount; x++) {
            models.add(generateSampleModel(x));
        }

        return models;
    }

    static List<Message> generateSampleMessages() {
        Message first = new Message();
        first.setResultCode(Activity.RESULT_OK);
        first.setPartNumber(1);
        first.generateRandomMessageID();
        first.setText(randomString());

        Message second = new Message();
        second.setPartNumber(2);
        second.setResultCode(SmsService.RESULT_MESSAGE_READY);
        second.generateRandomMessageID();
        second.setText(randomString());

        Message third = new Message();
        third.setPartNumber(2);
        third.setResultCode(SmsService.RESULT_MESSAGE_READY);
        third.generateRandomMessageID();
        third.setText(randomString());

        List<Message> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);

        return list;
    }

    private static String randomString() {
        StringBuilder sb = new StringBuilder(500);
        for (int i = 0; i < 500; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
