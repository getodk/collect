package org.odk.collect.projects

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken.getParameterized
import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.strings.UUIDGenerator
import java.util.function.Supplier

class SharedPreferencesProjectsRepository @JvmOverloads constructor(
    private val uuidGenerator: UUIDGenerator,
    private val gson: Gson,
    private val settings: Settings,
    private val key: String,
    private val clock: Supplier<Long> = Supplier { System.currentTimeMillis() }
) : ProjectsRepository {

    override fun get(uuid: String): Project.Saved? {
        return getAll().firstOrNull { it.uuid == uuid }
    }

    override fun getAll(): List<Project.Saved> {
        return getJsonProjects().sortedBy { it.createdAt }.map(JsonProject::toProject)
    }

    override fun save(project: Project): Project.Saved {
        val projects = getJsonProjects().toMutableList()

        when (project) {
            is Project.New -> {
                val projectToSave = project.toJson(uuidGenerator.generateUUID(), clock.get())
                projects.add(projectToSave)

                settings.save(key, gson.toJson(projects))
                return projectToSave.toProject()
            }

            is Project.Saved -> {
                val projectIndex = projects.indexOfFirst { it.uuid == project.uuid }
                if (projectIndex == -1) {
                    projects.add(project.toJson(clock.get()))
                } else {
                    projects[projectIndex] = project.toJson(projects[projectIndex].createdAt)
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

    private fun getJsonProjects(): List<JsonProject> {
        val projects = settings.getString(key)
        return if (projects != null && projects.isNotBlank()) {
            val typeToken = getParameterized(ArrayList::class.java, JsonProject::class.java)
            gson.fromJson<ArrayList<JsonProject>>(projects, typeToken.type)
        } else {
            emptyList()
        }
    }
}

// @SerializedName on every field keeps the JSON keys stable when obfuscation renames the fields
private data class JsonProject(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("color") val color: String,
    @SerializedName("createdAt") val createdAt: Long = 0, // Account for projects without timestamps (in older versions)
    @SerializedName("isOldGoogleDriveProject") val isOldGoogleDriveProject: Boolean
)

private fun JsonProject.toProject(): Project.Saved {
    return Project.Saved(this.uuid, this.name, this.icon, this.color, this.isOldGoogleDriveProject)
}

private fun Project.New.toJson(uuid: String, createdAt: Long): JsonProject {
    return JsonProject(uuid, this.name, this.icon, this.color, createdAt, false)
}

private fun Project.Saved.toJson(createdAt: Long): JsonProject {
    return JsonProject(uuid, this.name, this.icon, this.color, createdAt, this.isOldGoogleDriveProject)
}
