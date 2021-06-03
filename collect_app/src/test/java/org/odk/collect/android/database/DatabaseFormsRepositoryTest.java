package org.odk.collect.android.database;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;
import org.odk.collect.android.database.forms.DatabaseFormsRepository;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.formstest.FormsRepositoryTest;
import org.odk.collect.shared.TempFiles;

import java.io.File;
import java.util.function.Supplier;

@RunWith(AndroidJUnit4.class)
public class DatabaseFormsRepositoryTest extends FormsRepositoryTest {

    private final File dbDir = TempFiles.createTempDir();
    private final File formsDir = TempFiles.createTempDir();
    private final File cacheDir = TempFiles.createTempDir();

    @Override
    public FormsRepository buildSubject() {
        return new DatabaseFormsRepository(ApplicationProvider.getApplicationContext(), dbDir.getAbsolutePath(), formsDir.getAbsolutePath(), cacheDir.getAbsolutePath(), System::currentTimeMillis);
    }

    @Override
    public FormsRepository buildSubject(Supplier<Long> clock) {
        return new DatabaseFormsRepository(ApplicationProvider.getApplicationContext(), dbDir.getAbsolutePath(), formsDir.getAbsolutePath(), cacheDir.getAbsolutePath(), clock);
    }

    @Override
    public String getFormFilesPath() {
        return formsDir.getAbsolutePath();
    }
}
