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
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

/**
 * Emulates the process of copying a form via ADB
 */
public final class AdbFormLoadingUtils {

    private AdbFormLoadingUtils() {

    }

    /**
     * Copies a form with the given file name and given associated media to the SD Card.
     *
     * @param copyToDatabase if true the forms will be loaded into the database as if a form list
     *                       had been opened.
     */
    public static void copyFormToStorage(String formFilename, List<String> mediaFilePaths, boolean copyToDatabase, String copyTo, String projectName) throws IOException {
        copyForm(formFilename, copyTo, projectName);
        if (mediaFilePaths != null) {
            copyFormMediaFiles(formFilename, mediaFilePaths, projectName);
        }

        if (copyToDatabase) {
            new FormsDirDiskFormsSynchronizer().synchronize();
        }
    }

    public static void copyFormToStorage(String formFilename, String projectName) throws IOException {
        copyFormToStorage(formFilename, null, false, formFilename, projectName);
    }

    /**
     * Copies a form with the given file name to the SD Card where it will be loaded by
     * {@link FormLoaderTask}.
     */
    public static void copyFormToDemoProject(String formFilename) throws IOException {
        copyFormToStorage(formFilename, null, false, formFilename, "Demo project");
    }

    public static void copyFormToDemoProject(String formFilename, String copyTo) throws IOException {
        copyFormToStorage(formFilename, null, false, copyTo, "Demo project");
    }

    public static void copyFormToDemoProject(String formFilename, boolean copyToDatabase) throws IOException {
        copyFormToStorage(formFilename, null, copyToDatabase, formFilename, "Demo project");
    }

    public static void copyInstanceToDemoProject(String instanceFileName) throws IOException {
        String instanceDirPath = getInstancesDirPath("Demo project") + instanceFileName.split("\\.")[0];
        new File(instanceDirPath).mkdir();
        copyFileFromAssets("instances/" + instanceFileName, instanceDirPath + "/" + instanceFileName);
    }

    public static FormActivityTestRule getFormActivityTestRuleFor(String formFilename) {
        return new FormActivityTestRule(formFilename);
    }

    private static String copyForm(String formFilename, String copyTo, String projectName) throws IOException {
        String pathname = getFormsDirPath(projectName) + copyTo;
        copyFileFromAssets("forms/" + formFilename, pathname);
        return pathname;
    }

    private static void copyFormMediaFiles(String formFilename, List<String> mediaFilePaths, String projectName) throws IOException {
        String mediaPathName = getFormsDirPath(projectName) + formFilename.replace(".xml", "") + FileUtils.MEDIA_SUFFIX + "/";
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
    private static String getFormsDirPath(String projectName) {
        String path = getProjectPath(projectName) + "/forms/";
        new File(path).mkdirs();
        return path;
    }

    /**
     * @return the instances dir path that the user would expect (from docs)
     */
    private static String getInstancesDirPath(String projectName) {
        String path = getProjectPath(projectName) + "/instances/";
        new File(path).mkdirs();

        return path;
    }

    @NotNull
    private static String getProjectPath(String projectName) {
        File externalFilesDir = ApplicationProvider.<Application>getApplicationContext().getExternalFilesDir(null);
        String projectsDirPath = externalFilesDir + File.separator + "projects";

        if (projectName.equals("Demo project")) {
            return projectsDirPath + File.separator + "DEMO";
        } else {
            for (File projectDir : new File(projectsDirPath).listFiles()) {
                if (Arrays.stream(projectDir.listFiles()).anyMatch(file -> file.getName().equals(projectName))) {
                    return projectDir.getAbsolutePath();
                }
            }
        }

        throw new IllegalArgumentException("No project on disk with that name!");
    }
}
