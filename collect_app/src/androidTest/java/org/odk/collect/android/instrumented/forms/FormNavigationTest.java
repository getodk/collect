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

package org.odk.collect.android.instrumented.forms;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormDef;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.AdbFormLoadingUtils;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.RunnableRule;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.projects.Project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;

/**
 * This test has been created in order to check indices while navigating through a form.
 * It's especially important while navigating through a form that contains nested groups and if we
 * use groups with field-list appearance because in that case we need to collect all indices of
 * questions we want to display on one page (we need to recursively get all indices contained in
 * such a group and its children). It might be also tricky when navigating backwards because then we
 * need to navigate to an index of the first question of all we want to display on one page.
 */
@RunWith(Parameterized.class)
public class FormNavigationTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new RunnableRule(() -> {
                // Set up demo project
                AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
                component.projectImporter().importNewProject(Project.Companion.getDEMO_PROJECT());
                component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);
            }));

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        // Expected indices when swiping forward until the end of the form and back once.
        // An index of -1 indicates the start or end of a form.
        return Arrays.asList(
                ei("simpleFieldList.xml",
                        "-1, ", "0, ", "-1, ", "0, "),
                ei("fieldListInFieldList.xml",
                        "-1, ", "0, ", "-1, ", "0, "),
                ei("regularGroupWithFieldListGroupInside.xml",
                        "-1, ", "0, 0, ", "-1, ", "0, 0, "),
                ei("twoNestedRegularGroups.xml",
                        "-1, ", "0, 0, 0, ", "0, 0, 1, ", "0, 0, 2, ", "-1, ", "0, 0, 2, "),
                ei("regularGroupWithQuestionAndRegularGroupInside.xml",
                        "-1, ", "0, 0, ", "0, 1, 0, ", "0, 1, 1, ", "-1, ", "0, 1, 1, "),
                ei("regularGroupWithQuestionsAndRegularGroupInside.xml",
                        "-1, ", "0, 0, ", "0, 1, 0, ", "0, 2, ", "-1, ", "0, 2, "),
                ei("fieldListWithQuestionAndRegularGroupInside.xml",
                        "-1, ", "0, ", "-1, ", "0, "),
                ei("fieldListWithQuestionsAndRegularGroupsInside.xml",
                        "-1, ", "0, ", "-1, ", "0, "),
                ei("threeNestedFieldListGroups.xml",
                        "-1, ", "0, ", "-1, ", "0, "));
    }

    /**
     * Expected indices for each form
     */
    private static Object[] ei(String formName, String... expectedIndices) {
        return new Object[]{formName, expectedIndices};
    }

    private final String formName;
    private final String[] expectedIndices;

    public FormNavigationTest(String formName, String[] expectedIndices) {
        this.formName = formName;
        this.expectedIndices = expectedIndices;
    }

    @Test
    public void formNavigationTestCase() throws ExecutionException, InterruptedException {
        testIndices(formName, expectedIndices);
    }

    private void testIndices(String formName, String[] expectedIndices) throws ExecutionException, InterruptedException {
        try {
            copyToStorage(formName);
        } catch (IOException e) {
            Timber.i(e);
        }

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task, FormDef fd, String warningMsg) {
                try {
                    // For each form, simulate swiping forward through screens until the end of the
                    // form and then swiping back once. Verify the expected indices before and after each swipe.
                    for (int i = 0; i < expectedIndices.length - 1; i++) {
                        FormController formController = task.getFormController();
                        // check the current index
                        assertEquals(expectedIndices[i], formController.getFormIndex().toString());
                        if (i < expectedIndices.length - 2) {
                            formController.stepToNextScreenEvent();
                        } else {
                            formController.stepToPreviousScreenEvent();
                        }
                        // check the index again after navigating
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

    /**
     * FormLoaderTask loads forms from SD card so we need to put each form there
     */
    private void copyToStorage(String formName) throws IOException {
        AdbFormLoadingUtils.copyFormToStorage(formName);
    }

    private static String formPath(String formName) {
        return new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)
                + File.separator
                + formName;
    }
}
