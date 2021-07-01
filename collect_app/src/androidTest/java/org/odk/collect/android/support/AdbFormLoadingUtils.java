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

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

/**
 * Emulates the process of copying a form via ADB
 */
public class AdbFormLoadingUtils {

    private AdbFormLoadingUtils() {

    }

    /**
     * Copies a form with the given file name and given associated media to the SD Card.
     *
     * @param copyToDatabase if true the forms will be loaded into the database as if a form list
     *                       had been opened.
     */
    public static void copyFormToStorage(String formFilename, List<String> mediaFilePaths, boolean copyToDatabase, String copyTo) throws IOException {
        copyForm(formFilename, copyTo);
        if (mediaFilePaths != null) {
            copyFormMediaFiles(formFilename, mediaFilePaths);
        }

        if (copyToDatabase) {
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

    public static void copyFormToStorage(String formFilename, boolean copyToDatabase) throws IOException {
        copyFormToStorage(formFilename, null, copyToDatabase, formFilename);
    }

    public static void copyInstanceToStorage(String instanceFileName) throws IOException {
        String instanceDirPath = getInstancesDirPath() + instanceFileName.split("\\.")[0];
        new File(instanceDirPath).mkdir();
        copyFileFromAssets("instances/" + instanceFileName, instanceDirPath + "/" + instanceFileName);
    }

    public static FormActivityTestRule getFormActivityTestRuleFor(String formFilename) {
        return new FormActivityTestRule(formFilename);
    }

    private static String copyForm(String formFilename, String copyTo) throws IOException {
        String pathname = getFormsDirPath() + copyTo;
        copyFileFromAssets("forms/" + formFilename, pathname);
        return pathname;
    }

    private static void copyFormMediaFiles(String formFilename, List<String> mediaFilePaths) throws IOException {
        String mediaPathName = getFormsDirPath() + formFilename.replace(".xml", "") + FileUtils.MEDIA_SUFFIX + "/";
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

    /**
     * @return the forms dir path that the user would expect (from docs)
     */
    private static String getFormsDirPath() {
        return currentProjectPath() + "/forms/";
    }

    /**
     * @return the instances dir path that the user would expect (from docs)
     */
    private static String getInstancesDirPath() {
        return currentProjectPath() + "/instances/";
    }

    @NotNull
    private static String currentProjectPath() {
        return DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext()).storagePathProvider().getProjectRootDirPath();
    }
}
