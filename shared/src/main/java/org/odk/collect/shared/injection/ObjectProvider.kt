package org.odk.collect.shared.injection

import java.util.function.Supplier

/**
 * Allows a shared singleton to act as a provider for dependencies by implementing
 * [ObjectProviderHost] and returning a [ObjectProvider] from
 * [ObjectProviderHost.getObjectProvider].
 */

interface ObjectProvider {
    fun <T> provide(clazz: Class<T>): T
}

interface ObjectProviderHost {
    fun getObjectProvider(): ObjectProvider
}

/**
 * Provides a basic implementation of [ObjectProvider] that allows an instantiation of a
 * requested class to be provided by a [Supplier].
 */
class SupplierObjectProvider : ObjectProvider {

    private val suppliers: MutableMap<Class<*>, Supplier<*>> = mutableMapOf()

    fun <T> addSupplier(clazz: Class<T>, supplier: Supplier<T>) {
        suppliers[clazz] = supplier
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> provide(clazz: Class<T>): T {
        return suppliers[clazz]!!.get() as T
    }
}
