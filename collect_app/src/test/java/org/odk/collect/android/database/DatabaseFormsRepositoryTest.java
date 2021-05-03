package org.odk.collect.android.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.forms.DatabaseFormsRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.testshared.RobolectricHelpers;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.formstest.FormsRepositoryTest;

import java.util.function.Supplier;

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
        return new FormsRepositoryProvider().get();
    }

    @Override
    public FormsRepository buildSubject(Supplier<Long> clock) {
        return new DatabaseFormsRepository(clock, new StoragePathProvider(), DaggerUtils.getComponent(Collect.getInstance()).formsDatabaseProvider());
    }

    @Override
    public String getFormFilesPath() {
        return storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
    }
}
