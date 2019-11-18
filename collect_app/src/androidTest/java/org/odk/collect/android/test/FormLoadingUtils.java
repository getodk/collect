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

import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.odk.collect.android.forms.FormUtils.setupReferenceManagerForForm;
import static org.odk.collect.android.test.FileUtils.copyFileFromAssets;

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

        String pathname = copyForm(formFilename);
        if (mediaFilenames != null) {
            copyFormMediaFiles(formFilename, mediaFilenames);
        }

        setupReferenceManagerForForm(ReferenceManager.instance(), FileUtils.getFormMediaDir(new File(pathname)));
        saveFormToDatabase(new File(pathname));
    }

    /**
     * Copies a form with the given file name from the from the given assets folder to the SD Card
     * where it will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename) throws IOException {
        copyFormToSdCard(formFilename, null);
    }

    private static void saveFormToDatabase(File outFile) {
        Map<String, String> formInfo = FileUtils.getMetadataFromFormDefinition(outFile);
        final ContentValues v = new ContentValues();
        v.put(FormsColumns.FORM_FILE_PATH,          outFile.getAbsolutePath());
        v.put(FormsColumns.FORM_MEDIA_PATH,         FileUtils.constructMediaPath(outFile.getAbsolutePath()));
        v.put(FormsColumns.DISPLAY_NAME,            formInfo.get(FileUtils.TITLE));
        v.put(FormsColumns.JR_VERSION,              formInfo.get(FileUtils.VERSION));
        v.put(FormsColumns.JR_FORM_ID,              formInfo.get(FileUtils.FORMID));
        v.put(FormsColumns.SUBMISSION_URI,          formInfo.get(FileUtils.SUBMISSIONURI));
        v.put(FormsColumns.BASE64_RSA_PUBLIC_KEY,   formInfo.get(FileUtils.BASE64_RSA_PUBLIC_KEY));
        v.put(FormsColumns.AUTO_DELETE,             formInfo.get(FileUtils.AUTO_DELETE));
        v.put(FormsColumns.AUTO_SEND,               formInfo.get(FileUtils.AUTO_SEND));
        v.put(FormsColumns.GEOMETRY_XPATH,          formInfo.get(FileUtils.GEOMETRY_XPATH));

        new FormsDao().saveForm(v);
    }

    public static IntentsTestRule<FormEntryActivity> getFormActivityTestRuleFor(String formFilename) {
        return new FormActivityTestRule(formFilename);
    }

    private static String copyForm(String formFilename) throws IOException {
        String pathname = Collect.FORMS_PATH + "/" + formFilename;
        copyFileFromAssets("forms/" + formFilename, pathname);
        return pathname;
    }

    private static void copyFormMediaFiles(String formFilename, List<String> mediaFilenames) throws IOException {
        String mediaPathName = Collect.FORMS_PATH + "/" + formFilename.replace(".xml", "") + FileUtils.MEDIA_SUFFIX + "/";
        FileUtils.checkMediaPath(new File(mediaPathName));

        for (String mediaFilename : mediaFilenames) {
            copyFileFromAssets("media/" + mediaFilename, mediaPathName + mediaFilename);
        }
    }
}
