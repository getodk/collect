package org.odk.collect.android.forms;

import org.junit.Before;
import org.odk.collect.android.support.InMemFormsRepository;

import java.io.IOException;
import java.nio.file.Files;

public class InMemFormsRepositoryTest extends FormsRepositoryTest {

    private String tempDirectory;

    @Before
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("forms").toString();
    }

    @Override
    public FormsRepository buildSubject() {
        return new InMemFormsRepository();
    }

    @Override
    public String getFormFilesPath() {
        return tempDirectory;
    }
}
