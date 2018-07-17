package org.odk.collect.android.sms;

import android.content.Context;
import android.telephony.SmsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.sms.base.BaseSmsTest;
import org.odk.collect.android.sms.base.SampleData;
import org.odk.collect.android.tasks.sms.SmsSender;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
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
import static org.odk.collect.android.utilities.FileUtil.getSmsInstancePath;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SmsServiceTest extends BaseSmsTest {
    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    SmsManager smsManager;
    private StubSmsService smsService;
    @Inject
    InstancesDao instancesDao;
    @Inject
    FormsDao formsDao;
    @Inject
    RxEventBus eventBus;

    @Before
    public void setUp() {

        /*
         * Setting up dagger to utilize test dependencies across the app.
         */
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        testComponent.inject(this);

        setDefaultGateway();

        smsService = new StubSmsService(smsManager, submissionManager, instancesDao, RuntimeEnvironment.application, eventBus, formsDao);
    }

    @Test
    public void testSubmitForm() throws IOException {

        File dir = RuntimeEnvironment.application.getFilesDir();

        String instancePath = dir + "/test_instance";
        File file = new File(getSmsInstancePath(instancePath));

        String form = "+FN John +LN Doe +CTY London +G Male +ROLE Contractor +PIC image_243.png";

        writeFormToFile(form, file);

        FormInfo info = new FormInfo(instancePath, "", "");

        assertTrue(smsService.submitForm(SampleData.TEST_INSTANCE_ID, info, "Sample Form"));

        ShadowSmsManager.TextMultipartParams params = shadowOf(smsManager).getLastSentMultipartTextMessageParams();

        assertEquals(params.getDestinationAddress(), GATEWAY);
        assertNotNull(params.getSentIntents());
        assertNull(params.getDeliveryIntents());

        SmsSubmission result = submissionManager.getSubmissionModel(SampleData.TEST_INSTANCE_ID);

        //Check if all messages are currently being sent.
        assertEquals(params.getParts().size(), result.getMessages().size());

    }

    private void writeFormToFile(String form, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(form);
        }
    }

    class StubSmsService extends SmsService {

        StubSmsService(SmsManager smsManager, SmsSubmissionManagerContract smsSubmissionManager, InstancesDao instancesDao, Context context, RxEventBus rxEventBus, FormsDao formsDao) {
            super(smsManager, smsSubmissionManager, instancesDao, context, rxEventBus, formsDao);
        }

        /**
         * Overrides the default functionality by executing the SmsSender operation
         * that normally gets run when the job is started. This allows the operations of the job
         * to take place since the Job can't be run in test environments.
         *
         * @param instanceId from instanceDao
         */
        @Override
        protected void startSendMessagesJob(String instanceId) {
            new SmsSender(RuntimeEnvironment.application, instanceId).send();
        }
    }
}
