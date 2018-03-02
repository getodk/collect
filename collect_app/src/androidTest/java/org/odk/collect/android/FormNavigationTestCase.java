/*
 * Copyright 2018 Nafundi
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

package org.odk.collect.android;

import android.content.res.AssetManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.FormLoaderTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;

/**
 * This test has been created in order to check indices while navigating through a form.
 * It's especially important while navigate through a form which contains nested groups and if we
 * use groups with field-list appearance because in that case we need to collect all indices of
 * questions we want to display on one page (we need to recursively get all indices contained in
 * such a group and its children). It might be also tricky when navigate backwards because then we
 * need to navigate to an index of the first question of all we want to display on one page.
 */
@RunWith(Parameterized.class)
public class FormNavigationTestCase {

    private static final String FORMS_DIRECTORY = "/odk/forms/";

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"simpleFieldList.xml", new String[] {"-1, ", "0, ", "-1, ", "0, "}},
                {"fieldListInFieldList.xml", new String[] {"-1, ", "0, ", "-1, ", "0, "}},
                {"regularGroupWithFieldListGroupInside.xml", new String[] {"-1, ", "0, 0, ", "-1, ", "0, 0, "}},
                {"twoNestedRegularGroups.xml", new String[] {"-1, ", "0, 0, 0, ", "0, 0, 1, ", "0, 0, 2, ", "-1, ", "0, 0, 2, "}},
                {"regularGroupWithQuestionAndRegularGroupInside.xml", new String[] {"-1, ", "0, 0, ", "0, 1, 0, ", "0, 1, 1, ", "-1, ", "0, 1, 1, "}},
                {"regularGroupWithQuestionsAndRegularGroupInside.xml", new String[] {"-1, ", "0, 0, ", "0, 1, 0, ", "0, 2, ", "-1, ", "0, 2, "}},
                {"fieldListWithQuestionAndRegularGroupInside.xml", new String[] {"-1, ", "0, ", "-1, ", "0, "}},
                {"fieldListWithQuestionsAndRegularGroupsInside.xml", new String[] {"-1, ", "0, ", "-1, ", "0, "}},
                {"threeNestedFieldListGroups.xml", new String[] {"-1, ", "0, ", "-1, ", "0, "}}
        });
    }

    private String formName;
    private String[] expectedIndices;

    public FormNavigationTestCase(String formName, String[] expectedIndices) {
        this.formName = formName;
        this.expectedIndices = expectedIndices;
    }

    @Test
    public void formNavigationTestCase() throws ExecutionException, InterruptedException {
        testIndices(formName, expectedIndices);
    }

    private void testIndices(String formName, String[] expectedIndices) throws ExecutionException, InterruptedException {
        try {
            prepareFile(formName);
        } catch (IOException e) {
            Timber.i(e);
        }
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    for (int i = 0; i < expectedIndices.length - 1; i++) {
                        FormController formController = task.getFormController();
                        assertEquals(expectedIndices[i], formController.getFormIndex().toString());
                        if (i < expectedIndices.length - 2) {
                            formController.stepToNextScreenEvent();
                        } else {
                            formController.stepToPreviousScreenEvent();
                        }
                        assertEquals(expectedIndices[i + 1], formController.getFormIndex().toString());
                    }
                } catch (Exception e) {
                    Timber.i(e);
                }
            }

            @Override
            public void loadingError(String errorMsg) {
            }

            @Override
            public void onProgressStep(String stepMessage) {

            }
        });
        formLoaderTask.execute(formPath(formName)).get();
    }

    private void prepareFile(String formName) throws IOException {
        String pathname = formPath(formName);

        AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
        InputStream inputStream = assetManager.open("forms/formNavigationTestForms/" + formName);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    private static String formPath(String formName) {
        return Environment.getExternalStorageDirectory().getPath()
                + FORMS_DIRECTORY
                + formName;
    }
}