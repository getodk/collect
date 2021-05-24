package org.odk.collect.projects

const val NOT_SPECIFIED_UUID = "not_specified"

data class Project(
    val url: String,
    val username: String,
    val password: String,
    val name: String,
    val icon: String,
    val color: String,
    val uuid: String = NOT_SPECIFIED_UUID,
)
