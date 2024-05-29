package org.odk.collect.projects

/**
 * Some dependencies will end up being project specific and this provides a common interface
 * for constructing them.
 */
fun interface ProjectDependencyFactory<T> {
    fun create(projectId: String): T
}
