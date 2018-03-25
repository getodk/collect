/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.utilities;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import timber.log.Timber;

/**
 * Author: Meletis Margaritis
 * Date: 2/12/14
 * Time: 1:48 PM
 */
public final class ZipUtils {

    private ZipUtils() {

    }

    public static void unzip(File[] zipFiles) {
        for (File zipFile : zipFiles) {
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    doExtractInTheSameFolder(zipFile, zipInputStream, zipEntry);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private static void doExtractInTheSameFolder(File zipFile, ZipInputStream zipInputStream,
                                                 ZipEntry zipEntry) throws IOException {
        File targetFile;
        String fileName = zipEntry.getName();

        Timber.i("Found zipEntry with name: %s", fileName);

        if (fileName.contains("/") || fileName.contains("\\")) {
            // that means that this is a directory of a file inside a directory, so ignore it
            Timber.w("Ignored: %s", fileName);
            return;
        }

        // extract the new file
        targetFile = new File(zipFile.getParentFile(), fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
            IOUtils.copy(zipInputStream, fileOutputStream);
        }

        Timber.i("Extracted file \"%s\" out of %s", fileName, zipFile.getName());
    }
}
