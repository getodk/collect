package org.odk.collect.android.forms;

import org.junit.Before;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.InMemFormsRepository;

import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemFormsRepositoryTest extends FormsRepositoryTest {

    private final StoragePathProvider storagePathProvider = mock(StoragePathProvider.class);
    private String tempDirectory;

    @Before
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("forms").toString();
        when(storagePathProvider.getDirPath(StorageSubdirectory.FORMS)).thenReturn(tempDirectory);
        when(storagePathProvider.getFormDbPath(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    }

    @Override
    public FormsRepository buildSubject() {
        return new InMemFormsRepository();
    }

    @Override
    public String getFormsPath() {
        return tempDirectory;
    }

    @Override
    public Form.Builder getFormBuilder() {
        return new Form.Builder(storagePathProvider);
    }
}
