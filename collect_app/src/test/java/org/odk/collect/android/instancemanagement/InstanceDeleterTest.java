package org.odk.collect.android.instancemanagement;

import org.junit.Test;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.support.FormUtils;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.support.InMemInstancesRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.support.InstanceUtils.buildInstance;

public class InstanceDeleterTest {

    private final FormsRepository formsRepository = new InMemFormsRepository();
    private final InMemInstancesRepository instancesRepository = new InMemInstancesRepository();
    private final InstanceDeleter instanceDeleter = new InstanceDeleter(instancesRepository, formsRepository);

    @Test
    public void whenFormForInstanceIsSoftDeleted_andThereIsAnotherInstance_doesNotDeleteForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("version")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        instancesRepository.save(buildInstance("1", "version").build());
        instancesRepository.save(buildInstance("1", "version").build());

        Long id = instancesRepository.getAll().get(0).getId();
        instanceDeleter.delete(id);
        assertThat(formsRepository.getAll().size(), is(1));
    }

    @Test
    public void whenFormForInstanceIsSoftDeleted_andThereIsAnotherSoftDeletedInstance_deletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("version")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .id(1L)
                .jrFormId("1")
                .deletedDate(0L)
                .jrVersion("version")
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .id(2L)
                .jrFormId("1")
                .jrVersion("version")
                .build()
        );

        instanceDeleter.delete(2L);
        assertThat(formsRepository.getAll().size(), is(0));
    }

    @Test
    public void whenFormForInstanceIsSoftDeleted_andThereAreNoOtherInstances_deletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("version")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("version")
                .build()
        );

        instanceDeleter.delete(1L);
        assertThat(formsRepository.getAll().isEmpty(), is(true));
    }

    @Test
    public void whenFormForInstanceIsSoftDeleted_andThereAreNoOtherInstancesForThisVersion_deletesForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build()
        );

        formsRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("1")
                .jrVersion("2")
                .formFilePath(FormUtils.createXFormFile("1", "2").getAbsolutePath())
                .deleted(true)
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .id(2L)
                .jrFormId("1")
                .jrVersion("2")
                .build()
        );

        instanceDeleter.delete(1L);
        assertThat(formsRepository.getAll().size(), is(1));
        assertThat(formsRepository.getAll().get(0).getJrVersion(), is("2"));
    }

    @Test
    public void whenFormForInstanceIsNotSoftDeleted_andThereAreNoOtherInstances_doesNotDeleteForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("version")
                .deleted(false)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("version")
                .build()
        );

        instanceDeleter.delete(1L);
        assertThat(formsRepository.getAll().size(), is(1));
    }

    @Test
    public void whenFormVersionForInstanceIsNotSoftDeleted_andThereAreNoOtherInstances_doesNotDeleteForm() {
        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("1")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build()
        );

        formsRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("2")
                .deleted(false)
                .formFilePath(FormUtils.createXFormFile("1", "2").getAbsolutePath())
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .id(1L)
                .jrFormId("1")
                .jrVersion("2")
                .build()
        );

        instanceDeleter.delete(1L);
        assertThat(formsRepository.getAll().size(), is(2));
    }

    @Test
    public void whenInstanceIsSubmitted_softDeletesInstance() {
        Instance instance = instancesRepository.save(buildInstance("1", "version")
                .status(Instance.STATUS_SUBMITTED)
                .build());

        instanceDeleter.delete(instance.getId());
        assertThat(instancesRepository.get(instance.getId()).getDeletedDate(), notNullValue());
    }

    @Test
    public void whenInstanceIsSubmitted_clearsGeometryData() {
        Instance instance = instancesRepository.save(buildInstance("1", "version")
                .status(Instance.STATUS_SUBMITTED)
                .geometryType("Point")
                .geometry("{\"type\":\"Point\",\"coordinates\":[127.6, 11.1]}")
                .build());

        instanceDeleter.delete(instance.getId());

        Instance deletedInstance = instancesRepository.get(instance.getId());
        assertThat(deletedInstance.getGeometry(), nullValue());
        assertThat(deletedInstance.getGeometryType(), nullValue());
    }
}
