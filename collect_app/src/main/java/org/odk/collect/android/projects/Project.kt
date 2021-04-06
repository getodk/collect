package org.odk.collect.android.projects

data class Project(
    val name: String,
    val icon: String,
    val color: String
) {
    lateinit var uuid: String
}
