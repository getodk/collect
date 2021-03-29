package org.odk.collect.android.projects

import org.odk.collect.android.utilities.UUIDGenerator

class InMemProjectsRepository(private val uuidGenerator: UUIDGenerator) : ProjectsRepository {
    val projects = mutableListOf<Project>()

    override fun get(uuid: String) = projects.find { it.uuid == uuid }

    override fun getAll() = projects

    override fun add(projectName: String) {
        projects.add(Project(uuidGenerator.generateUUID(), projectName))
    }

    override fun delete(uuid: String) {
        projects.removeIf { it.uuid == uuid }
    }
}
