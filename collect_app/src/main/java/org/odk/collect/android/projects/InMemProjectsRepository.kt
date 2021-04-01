package org.odk.collect.android.projects

import org.odk.collect.android.utilities.UUIDGenerator

class InMemProjectsRepository(private val uuidGenerator: UUIDGenerator) : ProjectsRepository {
    val projects = mutableListOf<Project>()

    override fun get(uuid: String) = projects.find { it.uuid == uuid }

    override fun getAll() = projects

    override fun add(projectName: String, projectIcon: String, projectColor: String) {
        projects.add(Project(uuidGenerator.generateUUID(), projectName, projectIcon, projectColor))
    }

    override fun delete(uuid: String) {
        projects.removeIf { it.uuid == uuid }
    }
}
