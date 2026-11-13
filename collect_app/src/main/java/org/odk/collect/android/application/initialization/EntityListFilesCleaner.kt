package org.odk.collect.android.application.initialization

import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.entities.storage.getListNames
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.upgrade.Upgrade
import java.io.File

/**
 * Collect v2026.3.5 and earlier left a CSV copy of each downloaded entity list in the form's media
 * directory. Newer versions read a CSV in the media directory instead of the local entity list
 * with the same name (see https://github.com/getodk/collect/issues/6425), so these copies
 * would be read instead of the up-to-date entity list. They're never updated, so they need to be
 * deleted.
 */
class EntityListFilesCleaner(
    private val projectsRepository: ProjectsRepository,
    private val projectDependencyModuleFactory: ProjectDependencyFactory<ProjectDependencyModule>
) : Upgrade {
    override fun key() = MetaKeys.ENTITY_LIST_FILES_CLEANED

    override fun run() {
        projectsRepository.getAll().forEach { project ->
            val projectDependencyModule = projectDependencyModuleFactory.create(project.uuid)
            val lists = projectDependencyModule.entitiesRepository.getListNames()

            projectDependencyModule.formsRepository.getAll()
                .filter { it.usesEntities() }
                .forEach { form ->
                    lists.forEach { list -> File(form.formMediaPath, "$list.csv").delete() }
                }
        }
    }
}
