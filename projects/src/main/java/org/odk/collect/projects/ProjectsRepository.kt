package org.odk.collect.projects

interface ProjectsRepository {
    fun get(uuid: String): Project?

    fun getAll(): List<Project>

    fun add(project: Project)

    fun delete(uuid: String)

    fun deleteAll()
}
