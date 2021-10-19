package org.odk.collect.android.storage

import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.PathUtils
import timber.log.Timber
import java.io.File

class StoragePathProvider(
    private val currentProjectProvider: CurrentProjectProvider = DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider(),
    private val projectsRepository: ProjectsRepository = DaggerUtils.getComponent(Collect.getInstance()).projectsRepository(),
    val odkRootDirPath: String = Collect.getInstance().getExternalFilesDir(null)!!.absolutePath
) {

    @JvmOverloads
    fun getProjectRootDirPath(projectId: String? = null): String {
        val uuid = projectId ?: currentProjectProvider.getCurrentProject().uuid
        val path = getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + uuid

        if (!File(path).exists()) {
            File(path).mkdirs()

            try {
                val sanitizedProjectName = PathUtils.getPathSafeFileName(projectsRepository.get(uuid)!!.name)
                File(path + File.separator + sanitizedProjectName).createNewFile()
            } catch (e: Exception) {
                Timber.e(
                    FileUtils.getFilenameError(
                        projectsRepository.get(uuid)!!.name
                    )
                )
            }
        }

        return path
    }

    @JvmOverloads
    fun getOdkDirPath(subdirectory: StorageSubdirectory, projectId: String? = null): String {
        val path = when (subdirectory) {
            StorageSubdirectory.PROJECTS,
            StorageSubdirectory.SHARED_LAYERS -> odkRootDirPath + File.separator + subdirectory.directoryName
            StorageSubdirectory.FORMS,
            StorageSubdirectory.INSTANCES,
            StorageSubdirectory.CACHE,
            StorageSubdirectory.METADATA,
            StorageSubdirectory.LAYERS,
            StorageSubdirectory.SETTINGS -> getProjectRootDirPath(projectId) + File.separator + subdirectory.directoryName
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
