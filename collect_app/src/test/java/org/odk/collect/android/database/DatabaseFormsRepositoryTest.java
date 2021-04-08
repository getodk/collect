package org.odk.collect.android.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.forms.FormsRepositoryTest;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.utilities.Clock;

@RunWith(AndroidJUnit4.class)
public class DatabaseFormsRepositoryTest extends FormsRepositoryTest {

    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        RobolectricHelpers.mountExternalStorage();
        storagePathProvider = new StoragePathProvider();
        new StorageInitializer().createOdkDirsOnStorage();
    }

    @Override
    public FormsRepository buildSubject() {
        return new DatabaseFormsRepository();
    }

    @Override
    public FormsRepository buildSubject(Clock clock) {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public Clock providesClock() {
                return clock;
            }
        });

        return new DatabaseFormsRepository(clock);
    }

    @Override
    public String getFormFilesPath() {
        return storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
    }
}
