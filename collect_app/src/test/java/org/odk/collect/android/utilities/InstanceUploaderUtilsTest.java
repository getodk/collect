package org.odk.collect.android.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.support.FormUtils;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.support.InMemInstancesRepository;
import org.odk.collect.android.forms.Form;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
public class InstanceUploaderUtilsTest {
    /**
     * 1000 instances is a big number that would product a very long sql query that would cause
     * SQLiteException: Expression tree is too large if we didn't split it into parts.
     */
    private static final int NUMBER_OF_INSTANCES_TO_SEND = 1000;

    @Test
    public void shouldFormBeDeletedFunction_shouldReturnFalseIfAutoDeleteNotSpecifiedOnFormLevelAndDisabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, "1", "1", false), is(false));
    }

    @Test
    public void shouldFormBeDeletedFunction_shouldReturnTrueIfAutoDeleteNotSpecifiedOnFormLevelButEnabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, "1", "1", true), is(true));
    }

    @Test
    public void shouldFormBeDeletedFunction_shouldReturnFalseIfAutoDeleteSpecifiedAsFalseOnFormLevelButEnabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .autoDelete("false")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, "1", "1", true), is(false));
    }

    @Test
    public void shouldFormBeDeletedFunction_shouldReturnTrueIfAutoDeleteSpecifiedAsTrueOnFormLevelButDisabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .autoDelete("true")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, "1", "1", false), is(true));
    }

    @Test
    public void getUploadResultMessageTest() {
        assertThat(InstanceUploaderUtils.getUploadResultMessage(getTestInstancesRepository(), null, getTestUploadResult()),
                is(getExpectedResultMsg()));
    }

    @Test
    public void doesUrlRefersToGoogleSheetsFileTest() {
        assertThat(InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile("https://docs.google.com/spreadsheets/d/169qibpJCWgUy-SRtoyvKd1EKwV1nDfM0/edit#gid=773120038"), is(true));
        assertThat(InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile("https://drive.google.com/file/d/169qibpJCWgUy-SRtoyvKd1EKwV1nDfM0/edit#gid=773120038"), is(false));
    }

    private InMemInstancesRepository getTestInstancesRepository() {
        InMemInstancesRepository instancesRepository = new InMemInstancesRepository();

        for (int i = 1; i <= NUMBER_OF_INSTANCES_TO_SEND; i++) {
            long time = System.currentTimeMillis();
            Instance instance = new Instance.Builder()
                    .id((long) i)
                    .displayName("InstanceTest")
                    .jrFormId("instanceTest")
                    .status(Instance.STATUS_COMPLETE)
                    .lastStatusChangeDate(time)
                    .build();

            instancesRepository.save(instance);
        }
        return instancesRepository;
    }

    private Map<String, String> getTestUploadResult() {
        Map<String, String> result = new HashMap<>();
        for (int i = 1; i <= NUMBER_OF_INSTANCES_TO_SEND; i++) {
            result.put(String.valueOf(i), "full submission upload was successful!");
        }
        return result;
    }

    private String getExpectedResultMsg() {
        StringBuilder expectedResultMsg = new StringBuilder();
        for (int i = 1; i <= NUMBER_OF_INSTANCES_TO_SEND; i++) {
            expectedResultMsg.append("InstanceTest - Success\n\n");
        }
        return expectedResultMsg.toString().trim();
    }
}
