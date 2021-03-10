package org.odk.collect.android.tasks;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.database.DatabaseInstancesRepository;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.InstanceUtils;
import org.odk.collect.utilities.TestSettingsProvider;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class InstanceSyncTaskTest {

    @Test
    public void whenAnInstanceFileNoLongerExists_deletesFromDatabase() {
        createDirectoryInInstances();

        DatabaseInstancesRepository databaseInstancesRepository = new DatabaseInstancesRepository();
        databaseInstancesRepository.save(InstanceUtils.buildInstance("blah", "1").build());
        assertThat(databaseInstancesRepository.getAllNotDeleted().size(), is(1));

        InstanceSyncTask instanceSyncTask = new InstanceSyncTask(TestSettingsProvider.getSettingsProvider());
        instanceSyncTask.execute();

        assertThat(databaseInstancesRepository.getAllNotDeleted().size(), is(0));
    }

    /**
     * The task exits early if there is nothing in the instances dir which doesn't strictly make
     * sense - there could be instances in the DB that we need to delete
     */
    private void createDirectoryInInstances() {
        String odkDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES);
        new File(odkDirPath, "blah").mkdir();
    }
}
