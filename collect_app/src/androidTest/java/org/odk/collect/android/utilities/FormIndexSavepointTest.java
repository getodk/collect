/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.support.test.runner.AndroidJUnit4;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.SaveToDiskTask;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class FormIndexSavepointTest {

    private File instancePath;

    @Before
    public void setUp() {
        instancePath = new File(Collect.INSTANCES_PATH + File.separator + "test.xml");
        FormController formController = mock(FormController.class);
        when(formController.getInstancePath()).thenReturn(instancePath);
        Collect.getInstance().setFormController(formController);
    }

    @Test
    public void testBeginningOfForm() {
        FormIndex originalFormIndex = FormIndex.createBeginningOfFormIndex();
        File tempIndex = SaveToDiskTask.getFormIndexFile(instancePath.getName());
        FileUtils.exportFormIndexToFile(originalFormIndex, tempIndex);

        FormIndex readFormIndex = FileUtils.loadFormIndexFromFile();
        assertFormIndex(originalFormIndex, readFormIndex);
    }

    @Test
    public void testEndOfForm() {
        FormIndex originalFormIndex = FormIndex.createEndOfFormIndex();
        File tempIndex = SaveToDiskTask.getFormIndexFile(instancePath.getName());
        FileUtils.exportFormIndexToFile(originalFormIndex, tempIndex);

        FormIndex readFormIndex = FileUtils.loadFormIndexFromFile();
        assertFormIndex(originalFormIndex, readFormIndex);
    }

    @Test
    public void testNullReference() {
        FormIndex originalFormIndex = new FormIndex(1, 2, null);
        File tempIndex = SaveToDiskTask.getFormIndexFile(instancePath.getName());
        FileUtils.exportFormIndexToFile(originalFormIndex, tempIndex);

        FormIndex readFormIndex = FileUtils.loadFormIndexFromFile();
        assertFormIndex(originalFormIndex, readFormIndex);
    }

    @Test
    public void testNonNullReference() {
        TreeReference treeReference = TreeReference.rootRef();
        FormIndex originalFormIndex = new FormIndex(1, 2, treeReference);
        File tempIndex = SaveToDiskTask.getFormIndexFile(instancePath.getName());
        FileUtils.exportFormIndexToFile(originalFormIndex, tempIndex);

        FormIndex readFormIndex = FileUtils.loadFormIndexFromFile();
        assertFormIndex(originalFormIndex, readFormIndex);
    }

    private void assertFormIndex(FormIndex expected, FormIndex actual) {
        assertEquals(expected, actual);
        assertEquals(expected.getReference(), actual.getReference());
    }
}
