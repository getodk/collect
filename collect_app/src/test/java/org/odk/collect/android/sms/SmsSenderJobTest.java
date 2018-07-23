package org.odk.collect.android.sms;

import android.telephony.SmsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.odk.collect.android.sms.base.BaseSmsTest;
import org.odk.collect.android.sms.base.SampleData;
import org.odk.collect.android.tasks.sms.SmsSender;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSmsManager;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

/**
 * This test verifies that when a SMS Sender Job is added to the Job Manager Queue
 * the SmsSender functionality executes successfully by sending the message.
 * A Shadow instance of the SMSManager with the results of SMSManager from within the
 * job is then compared with the params passed to it to verify it's behaviour.
 */
@RunWith(RobolectricTestRunner.class)
public class SmsSenderJobTest extends BaseSmsTest {

    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    SmsManager smsManager;

    @Before
    public void setUp() {

        /**
         * Setting up dagger to utilize test dependencies across the app.
         */
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        testComponent.inject(this);

        setupSmsSubmissionManagerData();
        setDefaultGateway();
    }

    @Test
    public void smsSenderJobTest() {

        SmsSubmission model = submissionManager.getSubmissionModel(SampleData.TEST_INSTANCE_ID);

        SmsSender sender = new SmsSender(RuntimeEnvironment.application, model.getInstanceId());
        assertTrue(sender.send());

        ShadowSmsManager.TextMultipartParams params = shadowOf(smsManager).getLastSentMultipartTextMessageParams();

        assertEquals(params.getDestinationAddress(), GATEWAY);
        assertNotNull(params.getSentIntents());
        assertNull(params.getDeliveryIntents());

        SmsSubmission result = submissionManager.getSubmissionModel(SampleData.TEST_INSTANCE_ID);
        Message next = result.getNextUnsentMessage();

        //Check to see if the message was marked as sending by the job.
        assertTrue(next.isSending());
    }

    @Test
    public void sendUnsentMessageTest() {

        SmsSubmission model = submissionManager.getSubmissionModel(SampleData.TEST_UNSENT_MESSAGE_INSTANCE_ID);

        final Message message = model.getNextUnsentMessage();

        SmsSender sender = new SmsSender(RuntimeEnvironment.application, model.getInstanceId());
        assertTrue(sender.send());

        ShadowSmsManager.TextMultipartParams params = shadowOf(smsManager).getLastSentMultipartTextMessageParams();

        //Only one messaged failed so even though three messages are present only one should be sent.
        assertEquals(params.getSentIntents().size(), 1);

        //Only one part should exist.
        assertEquals(params.getParts().size(), 1);

        //The unsent message's text should be equal to the text that was sent.
        assertEquals(params.getParts().get(0), message.getText());
    }
}
