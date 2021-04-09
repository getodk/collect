package org.odk.collect.android.projects

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.Settings
import org.odk.collect.android.utilities.UUIDGenerator

class SharedPreferencesProjectsRepository(
    private val uuidGenerator: UUIDGenerator,
    private val gson: Gson,
    private val metaSettings: Settings
) : ProjectsRepository {

    override fun get(uuid: String): Project? {
        return getAll().firstOrNull() { it.uuid == uuid }
    }

    override fun getAll(): List<Project> {
        val projects = metaSettings.getString(MetaKeys.KEY_PROJECTS)
        return if (projects != null && projects.isNotBlank()) {
            gson.fromJson(projects, TypeToken.getParameterized(ArrayList::class.java, Project::class.java).type)
        } else {
            emptyList()
        }
    }

    override fun add(project: Project) {
        val projects = getAll().toMutableList().plus(project.copy(uuid = uuidGenerator.generateUUID()))
        metaSettings.save(MetaKeys.KEY_PROJECTS, gson.toJson(projects))
    }

    override fun delete(uuid: String) {
        val projects = getAll().toMutableList().minus(get(uuid))
        metaSettings.save(MetaKeys.KEY_PROJECTS, gson.toJson(projects))
    }

    override fun deleteAll() {
        metaSettings.remove(MetaKeys.KEY_PROJECTS)
    }
}
