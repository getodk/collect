package org.odk.collect.projects

import org.odk.collect.shared.strings.UUIDGenerator
import java.util.function.Supplier

class InMemProjectsRepository(
    private val uuidGenerator: UUIDGenerator = UUIDGenerator(),
    private val clock: Supplier<Long> = Supplier { System.currentTimeMillis() }
) : ProjectsRepository {

    val projects = mutableListOf<Project.Saved>()
    val timestamps = mutableListOf<Pair<String, Long>>()

    override fun get(uuid: String) = projects.find { it.uuid == uuid }

    override fun getAll() = timestamps.sortedBy { it.second }.map {
        projects.first { p -> p.uuid == it.first }
    }

    override fun save(project: Project): Project.Saved {
        when (project) {
            is Project.New -> {
                val projectToSave = Project.Saved(uuidGenerator.generateUUID(), project)
                projects.add(projectToSave)
                timestamps.add(Pair(projectToSave.uuid, clock.get()))
                return projectToSave
            }

            is Project.Saved -> {
                val projectIndex = projects.indexOf(get(project.uuid))
                if (projectIndex == -1) {
                    projects.add(project)
                    timestamps.add(Pair(project.uuid, clock.get()))
                } else {
                    projects[projectIndex] = project
                }

                return project
            }
        }
    }

    override fun delete(uuid: String) {
        projects.removeIf { it.uuid == uuid }
        timestamps.removeIf { it.first == uuid }
    }

    override fun deleteAll() {
        projects.clear()
        timestamps.clear()
    }
}
