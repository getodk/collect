package org.odk.collect.android.instancemanagement;

import org.junit.Test;
import org.odk.collect.android.support.FormUtils;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.forms.Form;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class InstanceSubmitterTest {
    @Test
    public void shouldFormBeSentFunction_shouldReturnFalseIfAutoSendNotSpecifiedOnFormLevelAndDisabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceSubmitter.shouldFormBeSent(formsRepository, "1", "1", false), is(false));
    }

    @Test
    public void shouldFormBeSentFunction_shouldReturnTrueIfAutoSendNotSpecifiedOnFormLevelButEnabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceSubmitter.shouldFormBeSent(formsRepository, "1", "1", true), is(true));
    }

    @Test
    public void shouldFormBeSentFunction_shouldReturnFalseIfAutoSendSpecifiedAsFalseOnFormLevelButEnabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .autoSend("false")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceSubmitter.shouldFormBeSent(formsRepository, "1", "1", true), is(false));
    }

    @Test
    public void shouldFormBeSentFunction_shouldReturnTrueIfAutoSendSpecifiedAsTrueOnFormLevelButDisabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .autoSend("true")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        assertThat(InstanceSubmitter.shouldFormBeSent(formsRepository, "1", "1", false), is(true));
    }

}
