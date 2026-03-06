package org.odk.collect.projects

/**
 * Some dependencies will end up being project specific and this provides a common interface
 * for constructing them.
 */
fun interface ProjectDependencyFactory<T> {
    fun create(projectId: String): T
}

fun <T> projectDependency(
    projectId: String,
    projectDependencyFactory: ProjectDependencyFactory<T>
): Lazy<T> {
    return projectDependency(projectId, projectDependencyFactory) { it }
}

fun <T, U> projectDependency(
    projectId: String,
    projectDependencyFactory: ProjectDependencyFactory<T>,
    transform: (T) -> U
): Lazy<U> {
    return lazy { transform(projectDependencyFactory.create(projectId)) }
}
