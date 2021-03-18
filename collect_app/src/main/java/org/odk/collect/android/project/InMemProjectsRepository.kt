package org.odk.collect.android.project

class InMemProjectsRepository : ProjectsRepository {
    val projects = mutableListOf(
        Project("1", "Turtle nesting", "odk_default", "turtlesarecool.com", "John", "1234", "T", "#4d79ff"),
        Project("2", "Polio - Banadir - Environment", "odk_default", "somalia.com", "Mark", "1234", "P", "#66ffcc"),
        Project("3", "Polio - Banadir - Daily reports", "odk_default", "somalia.com", "Anna", "1234", "P", "#66ffcc"),
        Project("4", "Measles - Banadir - Environment", "odk_default", "banadir.com", "Michael", "1234", "M", "#b300b3")
    )

    override fun get(uuid: String) = projects.find { it.uuid == uuid }

    override fun getAll() = projects

    override fun add(project: Project) {
        projects.add(project)
    }

    override fun delete(uuid: String) {
        projects.removeIf { it.uuid == uuid }
    }
}
