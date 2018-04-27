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
import org.odk.collect.android.sms.base.SampleData;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SmsServiceTest extends BaseSmsTest {
    @Inject
    JobManager jobManager;
    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    SmsManager smsManager;
    @Inject
    SmsService smsService;

    @Before
    public void setUp() {

        /**
         * Setting up dagger to utilize test dependencies across the app.
         */
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        testComponent.inject(this);

    }

    @Test
    public void testSubmitForm() throws IOException {

        File dir = RuntimeEnvironment.application.getApplicationContext().getFilesDir();

        File file = new File(dir + "/test_instance.txt");

        String form = "+FN John +LN Doe +CTY London +G Male +ROLE Contractor +PIC image_243.png";

        writeFormToFile(form, file);

        assertTrue(smsService.submitForm(SampleData.TEST_INSTANCE_ID, file.getPath()));
    }

    private void writeFormToFile(String form, File file) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        try {
            writer.write(form);
        } catch (IOException e) {
            writer.close();
        }
    }
}
