package org.odk.collect.android.sms;

import android.telephony.SmsManager;

import com.birbit.android.jobqueue.JobManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.odk.collect.android.sms.base.BaseSmsTest;
import org.odk.collect.android.sms.base.Event;
import org.odk.collect.android.sms.base.SampleData;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsJobMessage;
import org.odk.collect.android.tasks.sms.SmsSenderJob;
import org.odk.collect.android.tasks.sms.models.SmsSubmissionModel;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSmsManager;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

/**
 * This test verifies that when a SMS Sender Job is added to the Job Manager Queue
 * it executes successfully by sending the message.
 * A Shadow instance of the SMSManager with the results of SMSManager from within the
 * job is then compared with the params passed to it to verify it's behaviour.
 */
@RunWith(RobolectricTestRunner.class)
public class SmsSenderJobTest extends BaseSmsTest {

    @Inject
    JobManager jobManager;
    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    SmsManager smsManager;

    private static final String GATEWAY = "344-4545";

    @Before
    public void setUp() {

        /**
         * Setting up dagger to utilize test dependencies across the app.
         */
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        testComponent.inject(this);

        setupSmsSubmissionManagerData();
    }

    @Test
    public void smsSenderJobTest() throws InterruptedException {
        SmsSubmissionModel model = submissionManager.getSubmissionModel(SampleData.TEST_INSTANCE_ID);

        Message message = model.getNextUnsentMessage();

        SmsJobMessage jobMessage = new SmsJobMessage();
        jobMessage.setGateway(GATEWAY);
        jobMessage.setInstanceId(SampleData.TEST_INSTANCE_ID);
        jobMessage.setMessageId(message.getId());
        jobMessage.setText(message.getText());

        //suspends test execution until job is complete.
        CountDownLatch latch = new CountDownLatch(1);

        //This callback is triggered once the job has been run.
        jobManager.addCallback(new Event(job -> {
            ShadowSmsManager.TextSmsParams params = shadowOf(smsManager).getLastSentTextMessageParams();

            assertEquals(params.getDestinationAddress(), GATEWAY);
            assertNotNull(params.getSentIntent());
            assertEquals(params.getText(), message.getText());

            //should be null, no delivery intent was supplied.
            assertNull(params.getDeliveryIntent());

            SmsSubmissionModel result = submissionManager.getSubmissionModel(SampleData.TEST_INSTANCE_ID);
            Message next = result.getNextUnsentMessage();

            //Check to see if the message was marked as sending by the job.
            assertTrue(next.isSending());

            //resumes test execution.
            latch.countDown();
        }));

        //executes the Sms Sender as a background job.
        jobManager.addJobInBackground(new SmsSenderJob(jobMessage));
        jobManager.start();

        latch.await();
    }
}
