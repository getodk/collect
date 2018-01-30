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
import android.support.test.runner.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.FormLoaderTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class FormNavigationTestCase {

    private static final String FORMS_DIRECTORY = "/odk/forms/";

    @Test
    public void test1() throws IOException {
        String formName = "form1.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test2() throws IOException {
        String formName = "form2.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test3() throws IOException {
        String formName = "form3.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, 0, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test4() throws IOException {
        String formName = "form4.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 0, 0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 0, 1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 0, 2, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, 0, 2, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test5() throws IOException {
        String formName = "form5.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 1, 0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 1, 1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, 1, 1, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test6() throws IOException {
        String formName = "form6.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 1, 0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, 2, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, 2, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test7() throws IOException {
        String formName = "form7.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test8() throws IOException {
        String formName = "form8.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
    }

    @Test
    public void test9() throws IOException {
        String formName = "form9.xml";
        prepareFile(formName);

        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath(formName), null, null);
        formLoaderTask.setFormLoaderListener(new FormLoaderListener() {
            @Override
            public void loadingComplete(FormLoaderTask task) {
                try {
                    FormController formController = task.getFormController();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
                    formController.stepToNextScreenEvent();
                    assertEquals("-1, ", formController.getFormIndex().toString());
                    formController.stepToPreviousScreenEvent();
                    assertEquals("0, ", formController.getFormIndex().toString());
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
        formLoaderTask.execute(formPath(formName));
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