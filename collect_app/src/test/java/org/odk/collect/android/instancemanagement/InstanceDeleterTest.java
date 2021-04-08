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
                .formId("1")
                .version("version")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        instancesRepository.save(buildInstance("1", "version").build());
        instancesRepository.save(buildInstance("1", "version").build());

        Long id = instancesRepository.getAll().get(0).getDbId();
        instanceDeleter.delete(id);
        assertThat(formsRepository.getAll().size(), is(1));
    }

    @Test
    public void whenFormForInstanceIsSoftDeleted_andThereIsAnotherSoftDeletedInstance_deletesForm() {
        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("version")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .formId("1")
                .deletedDate(0L)
                .formVersion("version")
                .build()
        );

        Instance instanceToDelete = instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("version")
                .build()
        );

        instanceDeleter.delete(instanceToDelete.getDbId());
        assertThat(formsRepository.getAll().size(), is(0));
    }

    @Test
    public void whenFormForInstanceIsSoftDeleted_andThereAreNoOtherInstances_deletesForm() {
        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("version")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        Instance instanceToDelete = instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("version")
                .build()
        );

        instanceDeleter.delete(instanceToDelete.getDbId());
        assertThat(formsRepository.getAll().isEmpty(), is(true));
    }

    @Test
    public void whenFormForInstanceIsSoftDeleted_andThereAreNoOtherInstancesForThisVersion_deletesForm() {
        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("1")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build()
        );

        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("2")
                .formFilePath(FormUtils.createXFormFile("1", "2").getAbsolutePath())
                .deleted(true)
                .build()
        );

        Instance instanceToDelete = instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("1")
                .build()
        );

        instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("2")
                .build()
        );

        instanceDeleter.delete(instanceToDelete.getDbId());
        assertThat(formsRepository.getAll().size(), is(1));
        assertThat(formsRepository.getAll().get(0).getVersion(), is("2"));
    }

    @Test
    public void whenFormForInstanceIsNotSoftDeleted_andThereAreNoOtherInstances_doesNotDeleteForm() {
        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("version")
                .deleted(false)
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build()
        );

        Instance instanceToDelete = instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("version")
                .build()
        );

        instanceDeleter.delete(instanceToDelete.getDbId());
        assertThat(formsRepository.getAll().size(), is(1));
    }

    @Test
    public void whenFormVersionForInstanceIsNotSoftDeleted_andThereAreNoOtherInstances_doesNotDeleteForm() {
        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("1")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build()
        );

        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("2")
                .deleted(false)
                .formFilePath(FormUtils.createXFormFile("1", "2").getAbsolutePath())
                .build()
        );

        Instance instanceToDelete = instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("2")
                .build()
        );

        instanceDeleter.delete(instanceToDelete.getDbId());
        assertThat(formsRepository.getAll().size(), is(2));
    }

    @Test
    public void whenInstanceIsSubmitted_softDeletesInstance() {
        Instance instance = instancesRepository.save(buildInstance("1", "version")
                .status(Instance.STATUS_SUBMITTED)
                .build());

        instanceDeleter.delete(instance.getDbId());
        assertThat(instancesRepository.get(instance.getDbId()).getDeletedDate(), notNullValue());
    }

    @Test
    public void whenInstanceIsSubmitted_clearsGeometryData() {
        Instance instance = instancesRepository.save(buildInstance("1", "version")
                .status(Instance.STATUS_SUBMITTED)
                .geometryType("Point")
                .geometry("{\"type\":\"Point\",\"coordinates\":[127.6, 11.1]}")
                .build());

        instanceDeleter.delete(instance.getDbId());

        Instance deletedInstance = instancesRepository.get(instance.getDbId());
        assertThat(deletedInstance.getGeometry(), nullValue());
        assertThat(deletedInstance.getGeometryType(), nullValue());
    }
}
