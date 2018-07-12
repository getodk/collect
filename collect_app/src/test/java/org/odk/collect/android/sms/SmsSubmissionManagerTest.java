package org.odk.collect.android.sms;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.sms.base.BaseSmsTest;
import org.odk.collect.android.sms.base.SampleData;
import org.odk.collect.android.tasks.sms.SmsSubmissionManager;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.sms.base.SampleData.TEST_INSTANCE_ID;

@RunWith(RobolectricTestRunner.class)
public class SmsSubmissionManagerTest extends BaseSmsTest {

    private SmsSubmissionManager manager;

    @Before
    public void setup() {
        Context context = RuntimeEnvironment.application;
        manager = new SmsSubmissionManager(context);

        setupSmsSubmissionManagerData();
    }

    /***
     * Checks to see if the model that was persisted to shared preferences actually exists.
     */
    @Test
    public void getSubmissionTest() {
        SmsSubmission model = manager.getSubmissionModel(TEST_INSTANCE_ID);

        assertNotNull(model);
        assertEquals(model.getInstanceId(), TEST_INSTANCE_ID);
    }

    /**
     * Adds a model to the Submission Manager and then if it actually exists.
     */
    @Test
    public void addSubmissionTest() {

        /*
         * Clears all submissions so that the sample data can be re-added.
         */
        manager.clearSubmissions();

        SmsSubmission result = manager.getSubmissionModel(TEST_INSTANCE_ID);
        assertNull(result);

        manager.saveSubmission(SampleData.generateSampleModel());

        result = manager.getSubmissionModel(TEST_INSTANCE_ID);

        assertNotNull(result);
    }

    /**
     * Ensures that the model exists then tests deletion.
     */
    @Test
    public void deleteSubmissionTest() {

        SmsSubmission model = manager.getSubmissionModel(TEST_INSTANCE_ID);

        assertNotNull(model);

        manager.forgetSubmission(TEST_INSTANCE_ID);

        model = manager.getSubmissionModel(TEST_INSTANCE_ID);

        assertNull(model);
    }

    /**
     * Tests the sent logic attached to messages by checking if messages can be marked as sent
     * and also verifying that when they are marked as such they aren't returned when the next
     * message for submission is requested.
     */
    @Test
    public void testMarkMessageAsSent() {

        SmsSubmission model = manager.getSubmissionModel(TEST_INSTANCE_ID);
        /*
         * Gets the next unsent message which should be the second message since it's marked as unsent.
         */
        Message message = model.getNextUnsentMessage();

        assertEquals(model.getMessages().get(1).getId(), message.getId());

        assertTrue(manager.markMessageAsSent(TEST_INSTANCE_ID, message.getId()));

        model = manager.getSubmissionModel(TEST_INSTANCE_ID);
        /*
         * Grabs the next message for testing which should be the third message.
         */
        message = model.getNextUnsentMessage();

        assertEquals(message.getId(), model.getMessages().get(2).getId());
    }

    @Test
    public void markMessageAsSendingTest() {
        SmsSubmission model = manager.getSubmissionModel(TEST_INSTANCE_ID);

        Message message = model.getNextUnsentMessage();

        assertFalse(message.isSending());

        manager.markMessageAsSending(TEST_INSTANCE_ID, message.getId());

        model = manager.getSubmissionModel(TEST_INSTANCE_ID);

        message = model.getNextUnsentMessage();

        assertTrue(message.isSending());
    }
}
