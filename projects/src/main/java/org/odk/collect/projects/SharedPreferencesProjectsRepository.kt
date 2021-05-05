package org.odk.collect.projects

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.odk.collect.shared.Settings
import org.odk.collect.shared.UUIDGenerator

class SharedPreferencesProjectsRepository(
    private val uuidGenerator: UUIDGenerator,
    private val gson: Gson,
    private val settings: Settings,
    private val key: String
) : ProjectsRepository {

    override fun get(uuid: String): Project? {
        return getAll().firstOrNull { it.uuid == uuid }
    }

    override fun getAll(): List<Project> {
        val projects = settings.getString(key)
        return if (projects != null && projects.isNotBlank()) {
            gson.fromJson(projects, TypeToken.getParameterized(ArrayList::class.java, Project::class.java).type)
        } else {
            emptyList()
        }
    }

    override fun save(project: Project) {
        val projects = getAll().toMutableList()

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
        settings.save(key, gson.toJson(projects))
    }

    override fun delete(uuid: String) {
        val projects = getAll().toMutableList().minus(get(uuid))
        settings.save(key, gson.toJson(projects))
    }

    override fun deleteAll() {
        settings.remove(key)
    }
}
