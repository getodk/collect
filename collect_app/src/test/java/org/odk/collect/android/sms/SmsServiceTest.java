package org.odk.collect.android.sms;

import android.content.Context;
import android.telephony.SmsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.odk.collect.android.sms.base.BaseSmsTest;
import org.odk.collect.android.sms.base.SampleData;
import org.odk.collect.android.tasks.sms.SmsSender;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmissionModel;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSmsManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SmsServiceTest extends BaseSmsTest {
    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    SmsManager smsManager;
    StubSmsService smsService;
    @Mock
    InstancesDao instancesDao;

    @Before
    public void setUp() {

        /**
         * Setting up dagger to utilize test dependencies across the app.
         */
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        testComponent.inject(this);

        setDefaultGateway();

        smsService = new StubSmsService(smsManager, submissionManager, instancesDao, RuntimeEnvironment.application);
    }

    @Test
    public void testSubmitForm() throws IOException {

        File dir = RuntimeEnvironment.application.getFilesDir();

        File file = new File(dir + "/test_instance.txt");

        String form = "+FN John +LN Doe +CTY London +G Male +ROLE Contractor +PIC image_243.png";

        writeFormToFile(form, file);

        assertTrue(smsService.submitForm(SampleData.TEST_INSTANCE_ID, file.getAbsolutePath()));

        ShadowSmsManager.TextSmsParams params = shadowOf(smsManager).getLastSentTextMessageParams();

        assertEquals(params.getDestinationAddress(), GATEWAY);
        assertNotNull(params.getSentIntent());

        //should be null, no delivery intent was supplied.
        assertNull(params.getDeliveryIntent());

        SmsSubmissionModel result = submissionManager.getSubmissionModel(SampleData.TEST_INSTANCE_ID);
        Message next = result.getNextUnsentMessage();

        //The message is being sent so it should match the message of the SmsManager.
        assertEquals(params.getText(), next.getText());

        //Check to see if the message was marked as sending by the job.
        assertTrue(next.isSending());
    }

    private void writeFormToFile(String form, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(form);
        }
    }

    class StubSmsService extends SmsService {

        StubSmsService(SmsManager smsManager, SmsSubmissionManagerContract smsSubmissionManager, InstancesDao instancesDao, Context context) {
            super(smsManager, smsSubmissionManager, instancesDao, context);
        }

        /**
         * Overrides the default functionality by executing the SmsSender operation
         * that normally gets run when the job is started. This allows the operations of the job
         * to take place since the Job can't be run in test environments.
         *
         * @param instanceId from instanceDao
         */
        @Override
        protected void addMessageJobToQueue(String instanceId) {
            new SmsSender(RuntimeEnvironment.application, instanceId).send();
        }
    }
}
