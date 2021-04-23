package org.odk.collect.projects

const val NOT_SPECIFIED_UUID = "not_specified"

data class Project(
    val name: String,
    val icon: String,
    val color: String,
    val uuid: String = NOT_SPECIFIED_UUID,
)
