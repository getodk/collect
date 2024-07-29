package org.odk.collect.entities.javarosa.intance

import org.javarosa.core.model.instance.TreeElement
import org.javarosa.xform.parse.ExternalInstanceParser
import org.odk.collect.entities.storage.EntitiesRepository

internal class LocalEntitiesInstanceProvider(private val entitiesRepositoryProvider: () -> EntitiesRepository) :
    ExternalInstanceParser.InstanceProvider {

    override fun get(instanceId: String, instanceSrc: String): TreeElement {
        return get(instanceId, instanceSrc, false)
    }

    override fun get(instanceId: String, instanceSrc: String, partial: Boolean): TreeElement {
        val root = TreeElement("root", 0)
        createDataAdapter().getAll(instanceId, partial).forEach { root.addChild(it) }
        return root
    }

    override fun isSupported(instanceId: String, instanceSrc: String): Boolean {
        return createDataAdapter().supportsInstance(instanceId)
    }

    private fun createDataAdapter() = LocalEntitiesInstanceAdapter(entitiesRepositoryProvider())
}
