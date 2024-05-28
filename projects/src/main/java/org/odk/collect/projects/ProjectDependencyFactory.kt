package org.odk.collect.projects

/**
 * Some dependencies will end up being project specific and this provides a common interface
 * for constructing them.
 */
interface ProjectDependencyFactory<T> {
    fun create(projectId: String): T

    companion object {

        @JvmStatic
        fun <T> from(factory: (String) -> T): ProjectDependencyFactory<T> {
            return object : ProjectDependencyFactory<T> {
                override fun create(projectId: String): T {
                    return factory(projectId)
                }
            }
        }
    }
}
