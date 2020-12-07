package org.odk.collect.android.formmanagement;

import org.junit.Test;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.support.InMemInstancesRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.support.InstanceUtils.buildInstance;


public class FormDeleterTest {

    private final InMemFormsRepository formsRepository = new InMemFormsRepository();
    private final InMemInstancesRepository instancesRepository = new InMemInstancesRepository();
    private final FormDeleter formDeleter = new FormDeleter(formsRepository, instancesRepository);

    @Test
    public void whenFormHasDeletedInstances_deletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("id")
                .jrVersion("version")
                .build());

        instancesRepository.save(new Instance.Builder()
                .jrFormId("id")
                .jrVersion("version")
                .deletedDate(0L)
                .build());

        formDeleter.delete(1L);
        assertThat(formsRepository.getAll().size(), is(0));
    }

    @Test
    public void whenOtherVersionOfFormHasInstances_deletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("old")
                .build());

        formsRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("1")
                .jrVersion("new")
                .build());

        instancesRepository.save(new Instance.Builder()
                .jrFormId("1")
                .jrVersion("old")
                .build());

        formDeleter.delete(2L);
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getJrVersion(), is("old"));
    }

    @Test
    public void whenFormHasNullVersion_butAnotherVersionHasInstances_deletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("version")
                .build());

        formsRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("1")
                .jrVersion(null)
                .build());

        instancesRepository.save(new Instance.Builder()
                .jrFormId("1")
                .jrVersion("version")
                .build());

        formDeleter.delete(2L);
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getJrVersion(), is("version"));
    }

    @Test
    public void whenFormHasNullVersion_andInstancesWithNullVersion_softDeletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion(null)
                .build());

        instancesRepository.save(buildInstance(1L, "1", null).build());

        formDeleter.delete(1L);
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).isDeleted(), is(true));
    }

    @Test
    public void whenFormIdAndVersionCombinationIsNotUnique_andInstanceExists_hardDeletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("id")
                .jrVersion("version")
                .build());

        instancesRepository.save(new Instance.Builder()
                .jrFormId("id")
                .jrVersion("version")
                .build());

        formsRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("id")
                .jrVersion("version")
                .build());

        formDeleter.delete(1L);
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getId(), is(2L));
    }
}