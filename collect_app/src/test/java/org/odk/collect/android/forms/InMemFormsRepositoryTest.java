package org.odk.collect.android.forms;

import org.junit.Before;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.testshared.TempFiles;
import org.odk.collect.utilities.Clock;

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
    public FormsRepository buildSubject(Clock clock) {
        return new InMemFormsRepository(clock);
    }

    @Override
    public String getFormFilesPath() {
        return tempDirectory;
    }
}
