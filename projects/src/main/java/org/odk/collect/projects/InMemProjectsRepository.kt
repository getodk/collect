package org.odk.collect.projects

import org.odk.collect.shared.UUIDGenerator

class InMemProjectsRepository(private val uuidGenerator: UUIDGenerator) : ProjectsRepository {
    val projects = mutableListOf<Project>()

    override fun get(uuid: String) = projects.find { it.uuid == uuid }

    override fun getAll() = projects

    override fun save(project: Project) {
        if (project.uuid == NOT_SPECIFIED_UUID) {
            projects.add(project.copy(uuid = uuidGenerator.generateUUID()))
        } else {
            val projectIndex = projects.indexOf(get(project.uuid))
            if (projectIndex == -1) {
                projects.add(project)
            } else {
                projects.set(projectIndex, project)
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
