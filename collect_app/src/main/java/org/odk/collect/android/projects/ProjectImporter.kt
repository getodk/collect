package org.odk.collect.android.projects

import org.apache.commons.io.FileUtils.moveDirectoryToDirectory
import org.odk.collect.android.storage.StorageInitializer
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import java.io.File
import java.io.FileNotFoundException

class ProjectImporter(
    private val projectsRepository: ProjectsRepository,
    private val storageInitializer: StorageInitializer,
    private val storagePathProvider: StoragePathProvider
) {
    fun importDemoProject() {
        val project = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        projectsRepository.save(project)
        storageInitializer.createProjectDirsOnStorage(project)
    }

    fun importExistingProject(): Project.Saved {
        val project = projectsRepository.save(Project.New("Existing project", "E", "#3e9fcc"))

        try {
            val rootDir = storagePathProvider.odkRootDirPath
            listOf(
                File(rootDir, "forms"),
                File(rootDir, "instances"),
                File(rootDir, "metadata"),
                File(rootDir, "layers"),
                File(rootDir, ".cache"),
            ).forEach {
                val projectDir = File(storagePathProvider.getProjectRootDirPath(project))
                moveDirectoryToDirectory(it, projectDir, true)
            }
        } catch (_: FileNotFoundException) {
            storageInitializer.createProjectDirsOnStorage(project)
        }

        return project
    }

    companion object {
        const val DEMO_PROJECT_ID = "DEMO"
    }
}
