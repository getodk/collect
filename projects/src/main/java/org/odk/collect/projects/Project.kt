package org.odk.collect.projects

sealed class Project {
    abstract val name: String
    abstract val icon: String
    abstract val color: String

    data class New(
        override val name: String,
        override val icon: String,
        override val color: String
    ) : Project()

    data class Saved(
        val uuid: String,
        override val name: String,
        override val icon: String,
        override val color: String
    ) : Project() {

        constructor(uuid: String, project: New) : this(
            uuid,
            project.name,
            project.icon,
            project.color
        )
    }
}
