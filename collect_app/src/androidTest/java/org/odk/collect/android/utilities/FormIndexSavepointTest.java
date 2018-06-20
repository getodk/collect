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

import org.javarosa.core.model.FormIndex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.SaveFormIndexTask;
import org.odk.collect.android.tasks.SaveToDiskTask;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

// Verify that a FormIndex can be saved to and restored from a file
@RunWith(MockitoJUnitRunner.class)
public class FormIndexSavepointTest {

    @Mock
    private FormController formController;

    @Test
    public void saveAndReadFormIndexTest() {
        String instanceName = "test.xml";

        // for loadFormIndexFromFile
        File instancePath = new File(Collect.INSTANCES_PATH + File.separator + instanceName);
        when(formController.getInstanceFile()).thenReturn(instancePath);
        Collect.getInstance().setFormController(formController);

        FormIndex originalFormIndex = FormIndex.createBeginningOfFormIndex();
        File indexFile = SaveToDiskTask.getFormIndexFile(instanceName);
        SaveFormIndexTask.exportFormIndexToFile(originalFormIndex, indexFile);

        FormIndex readFormIndex = SaveFormIndexTask.loadFormIndexFromFile();

        assertEquals(originalFormIndex, readFormIndex);
        assertNotNull(readFormIndex);
        assertEquals(originalFormIndex.getReference(), readFormIndex.getReference());
    }
}
