package org.odk.collect.projects

data class NewProject(
    val username: String,
    val password: String,
    val name: String,
    val icon: String,
    val color: String
)
