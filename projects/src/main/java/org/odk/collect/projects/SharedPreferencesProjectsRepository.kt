package org.odk.collect.projects

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.odk.collect.shared.Settings
import org.odk.collect.shared.UUIDGenerator

class SharedPreferencesProjectsRepository(
    private val uuidGenerator: UUIDGenerator,
    private val gson: Gson,
    private val metaSettings: Settings,
    private val key: String
) : ProjectsRepository {

    override fun get(uuid: String): Project? {
        return getAll().firstOrNull { it.uuid == uuid }
    }

    override fun getAll(): List<Project> {
        val projects = metaSettings.getString(key)
        return if (projects != null && projects.isNotBlank()) {
            gson.fromJson(projects, TypeToken.getParameterized(ArrayList::class.java, Project::class.java).type)
        } else {
            emptyList()
        }
    }

    override fun add(project: Project) {
        val projects = if (project.uuid == NOT_SPECIFIED_UUID) {
            getAll().toMutableList().plus(project.copy(uuid = uuidGenerator.generateUUID()))
        } else {
            getAll().toMutableList().plus(project)
        }
        metaSettings.save(key, gson.toJson(projects))
    }

    override fun delete(uuid: String) {
        val projects = getAll().toMutableList().minus(get(uuid))
        metaSettings.save(key, gson.toJson(projects))
    }

    override fun deleteAll() {
        metaSettings.remove(key)
    }
}
