package org.odk.collect.android.project

data class Project(
    val uuid: String,
    val name: String,
    val serverType: String,
    val url: String,
    val username: String,
    val password: String,
    val icon: String,
    val color: String
)
