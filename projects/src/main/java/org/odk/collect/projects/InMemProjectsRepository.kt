package org.odk.collect.projects

import org.odk.collect.shared.UUIDGenerator

class InMemProjectsRepository(private val uuidGenerator: UUIDGenerator) : ProjectsRepository {
    val projects = mutableListOf<Project.Saved>()

    override fun get(uuid: String) = projects.find { it.uuid == uuid }

    override fun getAll() = projects

    override fun save(project: Project): Project.Saved {
        when (project) {
            is Project.New -> {
                val projectToSave = Project.Saved(uuidGenerator.generateUUID(), project)
                projects.add(projectToSave)
                return projectToSave
            }

            is Project.Saved -> {
                val projectIndex = projects.indexOf(get(project.uuid))
                if (projectIndex == -1) {
                    projects.add(project)
                } else {
                    projects[projectIndex] = project
                }

                return project
            }
        }
    }

    override fun delete(uuid: String) {
        projects.removeIf { it.uuid == uuid }
    }

    override fun deleteAll() {
        projects.clear()
    }
}
