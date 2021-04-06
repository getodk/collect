package org.odk.collect.android.projects

class InMemProjectsRepository() : ProjectsRepository {
    val projects = mutableListOf<Project>()

    override fun get(uuid: String) = projects.find { it.uuid == uuid }

    override fun getAll() = projects

    override fun add(project: Project) {
        projects.add(project)
    }

    override fun delete(uuid: String) {
        projects.removeIf { it.uuid == uuid }
    }
}
