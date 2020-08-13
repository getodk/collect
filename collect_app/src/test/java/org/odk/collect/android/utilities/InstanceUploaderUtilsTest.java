package org.odk.collect.android.utilities;

import org.junit.Test;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.support.InMemFormsRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class InstanceUploaderUtilsTest {
    @Test
    public void shouldFormBeSentFunction_shouldReturnFalseIfAutoSendNotSpecifiedOnFormLevelAndDisabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeSent(formsRepository, "1", "1", false), is(false));
    }

    @Test
    public void shouldFormBeSentFunction_shouldReturnTrueIfAutoSendNotSpecifiedOnFormLevelButEnabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeSent(formsRepository, "1", "1", true), is(true));
    }

    @Test
    public void shouldFormBeSentFunction_shouldReturnFalseIfAutoSendSpecifiedAsFalseOnFormLevelButEnabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .autoSend("false")
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeSent(formsRepository, "1", "1", true), is(false));
    }

    @Test
    public void shouldFormBeSentFunction_shouldReturnTrueIfAutoSendSpecifiedAsTrueOnFormLevelButDisabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .autoSend("true")
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeSent(formsRepository, "1", "1", false), is(true));
    }

    @Test
    public void shouldFormBeDeletedFunction_shouldReturnFalseIfAutoDeleteNotSpecifiedOnFormLevelAndDisabledInSettings() {
        InMemFormsRepository formsRepository = new InMemFormsRepository();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
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
                .build());

        assertThat(InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, "1", "1", false), is(true));
    }
}
