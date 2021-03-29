package org.odk.collect.android.projects

interface ProjectsRepository {
    fun get(uuid: String): Project?

    fun getAll(): List<Project>

    fun add(projectName: String)

    fun delete(uuid: String)
}
