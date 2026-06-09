package org.odk.collect.entities.javarosa.intance

import org.javarosa.core.model.instance.TreeElement
import org.javarosa.xform.parse.ExternalInstanceParser
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.forms.FormMediaFileRepository

internal class LocalEntitiesInstanceProvider(
    private val entitiesRepositoryProvider: () -> EntitiesRepository,
    private val mediaFileRepository: FormMediaFileRepository
) : ExternalInstanceParser.InstanceProvider {

    override fun get(instanceId: String, instanceSrc: String): TreeElement {
        return get(instanceId, instanceSrc, false)
    }

    override fun get(instanceId: String, instanceSrc: String, partial: Boolean): TreeElement {
        val root = TreeElement("root", 0)
        createDataAdapter().getAll(instanceId, partial).forEach { root.addChild(it) }
        return root
    }

    override fun isSupported(instanceId: String, instanceSrc: String): Boolean {
        // Entity lists are stored in the database, not as files on disk (their seed CSV is
        // deleted after import). So if a media file referenced by the instance's src exists,
        // it must be a plain attached CSV, which should be used instead of any same-named
        // entity list. Returning false lets JavaRosa parse the file directly.
        if (mediaFileRepository.get(instanceSrc)?.exists() == true) {
            return false
        }

        return createDataAdapter().supportsInstance(instanceId)
    }

    private fun createDataAdapter() = LocalEntitiesInstanceAdapter(entitiesRepositoryProvider())
}
