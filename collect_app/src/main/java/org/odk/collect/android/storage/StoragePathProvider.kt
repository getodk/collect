package org.odk.collect.android.storage

import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.shared.PathUtils.getAbsoluteFilePath
import org.odk.collect.shared.PathUtils.getRelativeFilePath
import java.io.File

class StoragePathProvider @JvmOverloads constructor(
    private val currentProjectProvider: CurrentProjectProvider = DaggerUtils.getComponent(Collect.getInstance())
        .currentProjectProvider(),
    val odkRootDirPath: String = Collect.getInstance().getExternalFilesDir(null)!!.absolutePath
) {

    fun getOdkRootDirPaths(): Array<String> {
        return arrayOf(getOdkDirPath(StorageSubdirectory.PROJECTS))
    }

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

    fun getProjectRootDirPath(): String {
        return getProjectRootDirPath(null)
    }

    fun getProjectRootDirPath(projectId: String?): String {
        return if (projectId == null) {
            val currentProjectId = currentProjectProvider.getCurrentProject().uuid
            getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + currentProjectId
        } else {
            getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + projectId
        }
    }

    @JvmOverloads
    fun getOdkDirPath(subdirectory: StorageSubdirectory, projectId: String? = null): String {
        return when (subdirectory) {
            StorageSubdirectory.FORMS,
            StorageSubdirectory.INSTANCES,
            StorageSubdirectory.CACHE,
            StorageSubdirectory.METADATA,
            StorageSubdirectory.LAYERS,
            StorageSubdirectory.SETTINGS -> getProjectRootDirPath(projectId) + File.separator + subdirectory.directoryName
            StorageSubdirectory.PROJECTS -> odkRootDirPath + File.separator + subdirectory.directoryName
        }
    }

    fun getCustomSplashScreenImagePath(): String {
        return odkRootDirPath + File.separator + "customSplashScreenImage.jpg"
    }

    fun getTmpImageFilePath(): String {
        return getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.jpg"
    }

    fun getTmpVideoFilePath(): String {
        return getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.mp4"
    }

    fun getRelativeInstancePath(filePath: String): String {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.INSTANCES), filePath)
    }

    fun getAbsoluteInstanceFilePath(filePath: String): String {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.INSTANCES), filePath)
    }

    fun getRelativeFormPath(filePath: String): String {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.FORMS), filePath)
    }

    fun getAbsoluteFormFilePath(filePath: String): String {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.FORMS), filePath)
    }

    fun getRelativeCachePath(filePath: String): String {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.CACHE), filePath)
    }

    fun getAbsoluteCacheFilePath(filePath: String): String {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.CACHE), filePath)
    }

    fun getRelativeMapLayerPath(filePath: String): String {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.LAYERS), filePath)
    }

    fun getAbsoluteOfflineMapLayerPath(filePath: String): String {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.LAYERS), filePath)
    }
}
