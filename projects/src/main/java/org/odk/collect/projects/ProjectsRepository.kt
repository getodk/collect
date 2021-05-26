package org.odk.collect.projects

interface ProjectsRepository {
    fun get(uuid: String): Project.Saved?

    fun getAll(): List<Project.Saved>

    fun save(project: Project): Project.Saved

    fun delete(uuid: String)

    fun deleteAll()
}
