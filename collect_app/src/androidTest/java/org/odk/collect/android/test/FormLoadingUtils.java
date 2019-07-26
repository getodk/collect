/*
 * Copyright 2019 Nafundi
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

package org.odk.collect.android.test;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

public class FormLoadingUtils {

    public static final String ALL_WIDGETS_FORM = "all-widgets.xml";

    private FormLoadingUtils() {

    }

    /**
     * Copies a form with the given file name and given associated media from the given assets
     * folder to the SD Card where it will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename, List<String> mediaFilenames) throws IOException {
        Collect.createODKDirs();
        copyForm(formFilename);

        if (mediaFilenames != null) {
            copyFormMediaFiles(formFilename, mediaFilenames);
        }
    }

    /**
     * Copies a form with the given file name from the from the given assets folder to the SD Card
     * where it will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename) throws IOException {
        copyFormToSdCard(formFilename, null);
    }

    private static void saveFormToDatabase(File outFile) {
        Map<String, String> formInfo = FileUtils.parseXML(outFile);
        final ContentValues v = new ContentValues();
        v.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH,          outFile.getAbsolutePath());
        v.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH,         FileUtils.constructMediaPath(outFile.getAbsolutePath()));
        v.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME,            formInfo.get(FileUtils.TITLE));
        v.put(FormsProviderAPI.FormsColumns.JR_VERSION,              formInfo.get(FileUtils.VERSION));
        v.put(FormsProviderAPI.FormsColumns.JR_FORM_ID,              formInfo.get(FileUtils.FORMID));
        v.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI,          formInfo.get(FileUtils.SUBMISSIONURI));
        v.put(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY,   formInfo.get(FileUtils.BASE64_RSA_PUBLIC_KEY));
        v.put(FormsProviderAPI.FormsColumns.AUTO_DELETE,             formInfo.get(FileUtils.AUTO_DELETE));
        v.put(FormsProviderAPI.FormsColumns.AUTO_SEND,               formInfo.get(FileUtils.AUTO_SEND));
        new FormsDao().saveForm(v);
    }

    public static IntentsTestRule<FormEntryActivity> getFormActivityTestRuleFor(String formFilename) {
        return new IntentsTestRule<FormEntryActivity>(FormEntryActivity.class) {
            @Override
            protected Intent getActivityIntent() {
                Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FormEntryActivity.class);
                intent.putExtra(EXTRA_TESTING_PATH, Collect.FORMS_PATH + "/" + formFilename);

                return intent;
            }

            @Override
            protected void afterActivityLaunched() {
                this.getActivity().setShouldOverrideAnimations(true);
                super.afterActivityLaunched();
            }
        };
    }

    private static void copyForm(String formFilename) throws IOException {
        String pathname = Collect.FORMS_PATH + "/" + formFilename;

        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        InputStream inputStream = assetManager.open("forms/" + formFilename);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);

        saveFormToDatabase(outFile);
    }

    private static void copyFormMediaFiles(String formFilename, List<String> mediaFilenames) throws IOException {
        String mediaPathName = Collect.FORMS_PATH + "/" + formFilename.replace(".xml", "") + FileUtils.MEDIA_SUFFIX + "/";
        FileUtils.checkMediaPath(new File(mediaPathName));

        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();

        for (String mediaFilename : mediaFilenames) {
            InputStream mediaInputStream = assetManager.open("media/" + mediaFilename);
            File mediaOutFile = new File(mediaPathName + mediaFilename);
            OutputStream mediaOutputStream = new FileOutputStream(mediaOutFile);

            IOUtils.copy(mediaInputStream, mediaOutputStream);
        }
    }
}