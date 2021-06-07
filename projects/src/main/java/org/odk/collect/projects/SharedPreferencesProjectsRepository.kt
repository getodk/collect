package org.odk.collect.projects

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.odk.collect.shared.Settings
import org.odk.collect.shared.strings.UUIDGenerator

class SharedPreferencesProjectsRepository(
    private val uuidGenerator: UUIDGenerator,
    private val gson: Gson,
    private val settings: Settings,
    private val key: String
) : ProjectsRepository {

    override fun get(uuid: String): Project.Saved? {
        return getAll().firstOrNull { it.uuid == uuid }
    }

    override fun getAll(): List<Project.Saved> {
        val projects = settings.getString(key)
        return if (projects != null && projects.isNotBlank()) {
            gson.fromJson(projects, TypeToken.getParameterized(ArrayList::class.java, Project.Saved::class.java).type)
        } else {
            emptyList()
        }
    }

    override fun save(project: Project): Project.Saved {
        val projects = getAll().toMutableList()

        when (project) {
            is Project.New -> {
                val projectToSave = Project.Saved(uuidGenerator.generateUUID(), project)
                projects.add(projectToSave)

                settings.save(key, gson.toJson(projects))
                return projectToSave
            }

            is Project.Saved -> {
                val projectIndex = projects.indexOf(get(project.uuid))
                if (projectIndex == -1) {
                    projects.add(project)
                } else {
                    projects[projectIndex] = project
                }

                settings.save(key, gson.toJson(projects))
                return project
            }
        }
    }

    override fun delete(uuid: String) {
        val projects = getAll().toMutableList().minus(get(uuid))
        settings.save(key, gson.toJson(projects))
    }

    override fun deleteAll() {
        settings.remove(key)
    }
}
