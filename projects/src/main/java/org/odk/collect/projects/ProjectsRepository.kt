package org.odk.collect.projects

interface ProjectsRepository {
    fun get(uuid: String): Project?

    fun getAll(): List<Project>

    fun save(project: Project)

    fun delete(uuid: String)

    fun deleteAll()
}
