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

package org.odk.collect.android.support;

import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.odk.collect.android.forms.FormUtils.setupReferenceManagerForForm;
import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

public class FormLoadingUtils {

    public static final String ALL_WIDGETS_FORM = "all-widgets.xml";

    private FormLoadingUtils() {

    }

    /**
     * Copies a form with the given file name and given associated media to the SD Card where it
     * will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToStorage(String formFilename, List<String> mediaFilePaths, boolean copyToDatabase, String copyTo) throws IOException {
        new StorageInitializer().createOdkDirsOnStorage();
        ReferenceManager.instance().reset();

        String pathname = copyForm(formFilename, copyTo);
        if (mediaFilePaths != null) {
            copyFormMediaFiles(formFilename, mediaFilePaths);
        }

        if (copyToDatabase) {
            setupReferenceManagerForForm(ReferenceManager.instance(), FileUtils.getFormMediaDir(new File(pathname)));
            new FormsDirDiskFormsSynchronizer().synchronize();
        }
    }

    /**
     * Copies a form with the given file name to the SD Card where it will be loaded by
     * {@link FormLoaderTask}.
     */
    public static void copyFormToStorage(String formFilename) throws IOException {
        copyFormToStorage(formFilename, null, false, formFilename);
    }

    public static void copyFormToStorage(String formFilename, String copyTo) throws IOException {
        copyFormToStorage(formFilename, null, false, copyTo);
    }

    public static IntentsTestRule<FormEntryActivity> getFormActivityTestRuleFor(String formFilename) {
        return new FormActivityTestRule(formFilename);
    }

    private static String copyForm(String formFilename, String copyTo) throws IOException {
        String pathname = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + "/" + copyTo;
        copyFileFromAssets("forms/" + formFilename, pathname);
        return pathname;
    }

    private static void copyFormMediaFiles(String formFilename, List<String> mediaFilePaths) throws IOException {
        String mediaPathName = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename.replace(".xml", "") + FileUtils.MEDIA_SUFFIX + "/";
        FileUtils.checkMediaPath(new File(mediaPathName));

        for (String mediaFilePath : mediaFilePaths) {
            copyFileFromAssets("media/" + mediaFilePath, mediaPathName + getMediaFileName(mediaFilePath));
        }
    }

    private static String getMediaFileName(String mediaFilePath) {
        return mediaFilePath.contains(File.separator)
                ? mediaFilePath.substring(mediaFilePath.indexOf(File.separator) + 1)
                : mediaFilePath;
    }
}
