package org.odk.collect.shared

import java.util.function.Supplier

interface MultiClassProviderHost {
    fun getMultiClassProvider(): MultiClassProvider
}

interface MultiClassProvider {
    fun <T> provide(clazz: Class<T>): T
}

class SupplierMultiClassProvider : MultiClassProvider {

    private val suppliers: MutableMap<Class<*>, Supplier<*>> = mutableMapOf()

    fun <T> addSupplier(clazz: Class<T>, supplier: Supplier<T>) {
        suppliers[clazz] = supplier
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> provide(clazz: Class<T>): T {
        return suppliers[clazz]!!.get() as T
    }
}
