package org.odk.collect.projects

data class Project(
    val name: String,
    val icon: String,
    val color: String,
    val uuid: String = "",
)
