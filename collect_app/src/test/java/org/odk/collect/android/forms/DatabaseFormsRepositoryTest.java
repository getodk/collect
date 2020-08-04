package org.odk.collect.android.forms;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;

@RunWith(AndroidJUnit4.class)
public class DatabaseFormsRepositoryTest extends FormsRepositoryTest {

    @Override
    public FormsRepository buildSubject() {
        RobolectricHelpers.mountExternalStorage();
        return new DatabaseFormsRepository();
    }
}
