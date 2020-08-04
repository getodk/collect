package org.odk.collect.android.forms;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;
import org.odk.collect.android.support.InMemFormsRepository;

@RunWith(AndroidJUnit4.class)
public class InMemFormsRepositoryTest extends FormsRepositoryTest {

    @Override
    public FormsRepository buildSubject() {
        return new InMemFormsRepository();
    }
}
