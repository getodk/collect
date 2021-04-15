package org.odk.collect.formstest;

import org.junit.Before;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.shared.TempFiles;

import java.util.function.Supplier;

public class InMemFormsRepositoryTest extends FormsRepositoryTest {

    private String tempDirectory;

    @Before
    public void setup() {
        tempDirectory = TempFiles.createTempDir().getAbsolutePath();
    }

    @Override
    public FormsRepository buildSubject() {
        return new InMemFormsRepository();
    }

    @Override
    public FormsRepository buildSubject(Supplier<Long> clock) {
        return new InMemFormsRepository(clock);
    }

    @Override
    public String getFormFilesPath() {
        return tempDirectory;
    }
}
