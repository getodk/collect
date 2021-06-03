package org.odk.collect.android.storage

import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import java.io.File

class StoragePathProvider {

    private val currentProjectProvider: CurrentProjectProvider
    val odkRootDirPath: String

    constructor() {
        currentProjectProvider =
            DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider()
        odkRootDirPath = Collect.getInstance().getExternalFilesDir(null)!!.absolutePath
    }

    constructor(currentProjectProvider: CurrentProjectProvider, externalFilesDirPath: String) {
        this.currentProjectProvider = currentProjectProvider
        odkRootDirPath = externalFilesDirPath
    }

    val odkRootDirPaths: Array<String>
        get() = arrayOf(
            getOdkDirPath(StorageSubdirectory.PROJECTS)
        )

    fun getProjectDirPaths(projectId: String?): Array<String> {
        return arrayOf(
            getOdkDirPath(StorageSubdirectory.FORMS, projectId),
            getOdkDirPath(StorageSubdirectory.INSTANCES, projectId),
            getOdkDirPath(StorageSubdirectory.CACHE, projectId),
            getOdkDirPath(StorageSubdirectory.METADATA, projectId),
            getOdkDirPath(StorageSubdirectory.LAYERS, projectId),
            getOdkDirPath(StorageSubdirectory.SETTINGS, projectId)
        )
    }

    val projectRootDirPath: String
        get() = getProjectRootDirPath(null)

    fun getProjectRootDirPath(projectId: String?): String {
        return if (projectId == null) {
            val currentProjectId = currentProjectProvider.getCurrentProject().uuid
            getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + currentProjectId
        } else {
            getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + projectId
        }
    }

    fun getOdkDirPath(subdirectory: StorageSubdirectory): String {
        return getOdkDirPath(subdirectory, null)
    }

    fun getOdkDirPath(subdirectory: StorageSubdirectory, projectId: String?): String {
        return when (subdirectory) {
            StorageSubdirectory.FORMS, StorageSubdirectory.INSTANCES, StorageSubdirectory.CACHE, StorageSubdirectory.METADATA, StorageSubdirectory.LAYERS, StorageSubdirectory.SETTINGS -> getProjectRootDirPath(
                projectId
            ) + File.separator + subdirectory.directoryName
            StorageSubdirectory.PROJECTS -> odkRootDirPath + File.separator + subdirectory.directoryName
            else -> throw IllegalStateException("Unexpected value: $subdirectory")
        }
    }

    val customSplashScreenImagePath: String
        get() = odkRootDirPath + File.separator + "customSplashScreenImage.jpg"
    val tmpImageFilePath: String
        get() = getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.jpg"
    val tmpVideoFilePath: String
        get() = getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.mp4"

    fun getRelativeInstancePath(filePath: String?): String? {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.INSTANCES), filePath)
    }

    fun getAbsoluteInstanceFilePath(filePath: String?): String? {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.INSTANCES), filePath)
    }

    fun getRelativeFormPath(filePath: String?): String? {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.FORMS), filePath)
    }

    fun getAbsoluteFormFilePath(filePath: String?): String? {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.FORMS), filePath)
    }

    fun getRelativeCachePath(filePath: String?): String? {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.CACHE), filePath)
    }

    fun getAbsoluteCacheFilePath(filePath: String?): String? {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.CACHE), filePath)
    }

    fun getRelativeMapLayerPath(filePath: String?): String? {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.LAYERS), filePath)
    }

    fun getAbsoluteOfflineMapLayerPath(filePath: String?): String? {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.LAYERS), filePath)
    }

    companion object {

        @JvmStatic
        fun getRelativeFilePath(dirPath: String, filePath: String?): String? {
            if (filePath == null || filePath.isEmpty()) {
                return null
            }
            return if (filePath.startsWith(dirPath)) filePath.substring(dirPath.length + 1) else filePath
        }

        @JvmStatic
        fun getAbsoluteFilePath(dirPath: String, filePath: String?): String? {
            if (filePath == null || filePath.isEmpty()) {
                return null
            }
            return if (filePath.startsWith(dirPath)) filePath else dirPath + File.separator + filePath
        }
    }
}
