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
package org.odk.collect.android.support

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.odk.collect.android.formmanagement.LocalFormUseCases
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.FileUtils
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Arrays

/**
 * Emulates the process of copying a form via ADB
 */
object StorageUtils {
    /**
     * Copies a form with the given file name and given associated media to the SD Card.
     *
     * @param copyToDatabase if true the forms will be loaded into the database as if a form list
     * had been opened.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun copyFormToStorage(
        formFilename: String,
        mediaFilePaths: List<String>? = null,
        copyToDatabase: Boolean = false,
        copyTo: String = formFilename,
        projectName: String = "Demo project"
    ) {
        copyForm(formFilename, copyTo, projectName)
        if (mediaFilePaths != null) {
            copyFormMediaFiles(formFilename, mediaFilePaths, projectName)
        }

        if (copyToDatabase) {
            val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
            val formsRepository = component.formsRepositoryProvider().create()
            val formsDir = component.storagePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)
            LocalFormUseCases.synchronizeWithDisk(formsRepository, formsDir)
        }
    }

    /**
     * Copies a form with the given file name to the SD Card where it will be loaded by
     * [FormLoaderTask].
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun copyFormToDemoProject(formFilename: String, mediaFilePaths: List<String>? = null, copyToDatabase: Boolean = false, copyTo: String? = null) {
        copyFormToStorage(formFilename, mediaFilePaths, copyToDatabase, copyTo ?: formFilename, "Demo project")
    }

    @JvmStatic
    @Throws(IOException::class)
    fun copyFormToDemoProject(formFilename: String, copyTo: String?) {
        copyFormToDemoProject(formFilename, null, copyTo = copyTo)
    }

    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun copyInstance(instanceFileName: String, projectName: String) {
        val instanceDirPath = getInstancesDirPath(projectName) + instanceFileName.split("\\.".toRegex()).toTypedArray()[0]
        File(instanceDirPath).mkdir()
        FileUtils.copyFileFromAssets(
            InstrumentationRegistry.getInstrumentation().context,
            "$instanceDirPath/$instanceFileName",
            "instances/$instanceFileName"
        )
    }

    @Throws(IOException::class)
    private fun copyForm(formFilename: String, copyTo: String, projectName: String): String {
        val pathname = getFormsDirPath(projectName) + copyTo
        FileUtils.copyFileFromResources(
            "forms/$formFilename",
            pathname
        )
        return pathname
    }

    @Throws(IOException::class)
    private fun copyFormMediaFiles(formFilename: String, mediaFilePaths: List<String>, projectName: String) {
        val mediaPathName = getFormsDirPath(projectName) + formFilename.replace(".xml", "") + FileUtils.MEDIA_SUFFIX + "/"
        FileUtils.checkMediaPath(File(mediaPathName))
        for (mediaFilePath in mediaFilePaths) {
            FileUtils.copyFileFromResources(
                "media/$mediaFilePath",
                mediaPathName + getMediaFileName(mediaFilePath)
            )
        }
    }

    private fun getMediaFileName(mediaFilePath: String): String {
        return if (mediaFilePath.contains(File.separator)) mediaFilePath.substring(mediaFilePath.indexOf(File.separator) + 1) else mediaFilePath
    }

    /**
     * @return the forms dir path that the user would expect (from docs)
     */
    private fun getFormsDirPath(projectName: String): String {
        val path = getProjectPath(projectName) + "/forms/"
        File(path).mkdirs()
        return path
    }

    /**
     * @return the instances dir path that the user would expect (from docs)
     */
    @JvmStatic
    @JvmOverloads
    fun getInstancesDirPath(projectName: String = "Demo project"): String {
        val path = getProjectPath(projectName) + "/instances/"
        File(path).mkdirs()
        return path
    }

    private fun getProjectPath(projectName: String): String {
        val externalFilesDir = ApplicationProvider.getApplicationContext<Application>().getExternalFilesDir(null)
        val projectsDirPath = externalFilesDir.toString() + File.separator + "projects"
        if (projectName == "Demo project") {
            return projectsDirPath + File.separator + "DEMO"
        } else {
            for (projectDir in File(projectsDirPath).listFiles()) {
                if (Arrays.stream(projectDir.listFiles()).anyMatch { file: File -> file.name == projectName }) {
                    return projectDir.absolutePath
                }
            }
        }
        throw IllegalArgumentException("No project on disk with that name!")
    }

    @Throws(IOException::class)
    @JvmStatic
    fun getAuditLogForFirstInstance(): List<CSVRecord> {
        val instanceDir = File(getInstancesDirPath()).listFiles()[0]
        val auditLog = Arrays.stream(instanceDir.listFiles())
            .filter { file: File -> file.name == "audit.csv" }
            .findFirst()
            .get()

        val records = FileReader(auditLog).use { auditLogReader ->
            CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(auditLogReader).use { parser ->
                parser.records
            }
        }

        return records
    }
}
