package org.odk.collect.android.tasks;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.formstest.FormUtils;
import org.odk.collect.formstest.InstanceUtils;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class InstanceSyncTaskTest {

    @Before
    public void setup() {
        CollectHelpers.setupDemoProject();
    }


    @Test
    public void whenAnInstanceFileNoLongerExists_deletesFromDatabase() {
        createDirectoryInInstances();

        InstancesRepository instancesRepository = new InstancesRepositoryProvider().get();
        instancesRepository.save(InstanceUtils.buildInstance("blah", "1", new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES)).build());
        assertThat(instancesRepository.getAllNotDeleted().size(), is(1));

        InstanceSyncTask instanceSyncTask = new InstanceSyncTask(TestSettingsProvider.getSettingsProvider());
        instanceSyncTask.doInBackground();

        assertThat(instancesRepository.getAllNotDeleted().size(), is(0));
    }

    @Test
    public void whenAnInstanceFileExistsOnDisk_andNotInDatabase_addsToDatabase() {
        createInstanceOnDisk(ONE_QUESTION_INSTANCE);
        new FormsRepositoryProvider().get().save(FormUtils.buildForm("one_question", "1", new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS))
                .build()
        );

        InstanceSyncTask instanceSyncTask = new InstanceSyncTask(TestSettingsProvider.getSettingsProvider());
        instanceSyncTask.doInBackground();

        List<Instance> instances = new InstancesRepositoryProvider().get().getAllByFormId("one_question");
        assertThat(instances.size(), is(1));
        assertThat(instances.get(0).getFormId(), is("one_question"));
        assertThat(instances.get(0).getFormVersion(), is("1"));
        assertThat(instances.get(0).getStatus(), is(Instance.STATUS_COMPLETE));
    }

    /**
     * The task exits early if there is nothing in the instances dir which doesn't strictly make
     * sense - there could be instances in the DB that we need to delete
     */
    private void createDirectoryInInstances() {
        String odkDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES);
        new File(odkDirPath, "blah").mkdir();
    }

    private void createInstanceOnDisk(String instance) {
        String odkDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES);

        File instanceDir = new File(odkDirPath, "instance");
        instanceDir.mkdir();

        File instanceFile = new File(instanceDir, "instance.xml");
        FileUtils.write(instanceFile, instance.getBytes());
    }

    public static final String ONE_QUESTION_INSTANCE = "<?xml version='1.0' ?><data id=\"one_question\" orx:version=\"1\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:orx=\"http://openrosa.org/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><age /></data>";
}
