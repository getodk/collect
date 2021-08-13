package org.odk.collect.android.storage

import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import java.io.File

class StoragePathProvider @JvmOverloads constructor(
    private val currentProjectProvider: CurrentProjectProvider = DaggerUtils.getComponent(Collect.getInstance())
        .currentProjectProvider(),
    val odkRootDirPath: String = Collect.getInstance().getExternalFilesDir(null)!!.absolutePath
) {

    @JvmOverloads
    fun getProjectRootDirPath(projectId: String? = null): String {
        val path = if (projectId == null) {
            val currentProjectId = currentProjectProvider.getCurrentProject().uuid
            getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + currentProjectId
        } else {
            getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + projectId
        }

        if (!File(path).exists()) {
            File(path).mkdirs()
        }

        return path
    }

    @JvmOverloads
    fun getOdkDirPath(subdirectory: StorageSubdirectory, projectId: String? = null): String {
        val path = when (subdirectory) {
            StorageSubdirectory.FORMS,
            StorageSubdirectory.INSTANCES,
            StorageSubdirectory.CACHE,
            StorageSubdirectory.METADATA,
            StorageSubdirectory.LAYERS,
            StorageSubdirectory.SETTINGS -> getProjectRootDirPath(projectId) + File.separator + subdirectory.directoryName
            StorageSubdirectory.PROJECTS -> odkRootDirPath + File.separator + subdirectory.directoryName
        }

        if (!File(path).exists()) {
            File(path).mkdirs()
        }

        return path
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
}
