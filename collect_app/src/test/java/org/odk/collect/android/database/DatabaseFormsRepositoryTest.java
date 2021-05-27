package org.odk.collect.android.database;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.odk.collect.android.database.forms.DatabaseFormsRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.formstest.FormsRepositoryTest;
import org.odk.collect.shared.TempFiles;

import java.io.File;
import java.util.function.Supplier;

@RunWith(AndroidJUnit4.class)
public class DatabaseFormsRepositoryTest extends FormsRepositoryTest {

    private StoragePathProvider storagePathProvider;
    private final File tempDir = TempFiles.createTempDir();

    @Before
    public void setup() {
        CollectHelpers.setupDemoProject();
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
        storagePathProvider = component.storagePathProvider();
    }

    @Override
    public FormsRepository buildSubject() {
        return new DatabaseFormsRepository(ApplicationProvider.getApplicationContext(), tempDir.getAbsolutePath(), System::currentTimeMillis, storagePathProvider);
    }

    @Override
    public FormsRepository buildSubject(Supplier<Long> clock) {
        return new DatabaseFormsRepository(ApplicationProvider.getApplicationContext(), tempDir.getAbsolutePath(), clock, storagePathProvider);
    }

    @Override
    public String getFormFilesPath() {
        return storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
    }
}
