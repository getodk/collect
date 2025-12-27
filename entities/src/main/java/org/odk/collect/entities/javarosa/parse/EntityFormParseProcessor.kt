package org.odk.collect.entities.javarosa.parse

import org.javarosa.core.model.DataBinding
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.model.xform.XPathReference
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParser.BindAttributeProcessor
import org.javarosa.xform.parse.XFormParser.FormDefProcessor
import org.javarosa.xform.parse.XFormParser.MissingModelAttributeException
import org.javarosa.xform.parse.XFormParser.ModelAttributeProcessor
import org.odk.collect.entities.javarosa.parse.EntityFormParseProcessor.Companion.VERSIONS.V2022_1
import org.odk.collect.entities.javarosa.parse.EntityFormParseProcessor.Companion.VERSIONS.V2023_1
import org.odk.collect.entities.javarosa.parse.EntityFormParseProcessor.Companion.VERSIONS.V2024_1
import org.odk.collect.entities.javarosa.parse.EntityFormParseProcessor.Companion.VERSIONS.V2025_1
import org.odk.collect.entities.javarosa.spec.EntityFormParser
import org.odk.collect.entities.javarosa.spec.UnrecognizedEntityVersionException

class EntityFormParseProcessor() : BindAttributeProcessor, FormDefProcessor, ModelAttributeProcessor {
    private val saveTos = mutableListOf<SaveTo>()
    private var version: String? = null

    override fun getModelAttributes(): Set<Pair<String, String>> {
        return setOf(Pair(ENTITIES_NAMESPACE, "entities-version"))
    }

    @Throws(XFormParser.ParseException::class)
    override fun processModelAttribute(name: String, value: String) {
        version = value

        if (SUPPORTED_VERSIONS.none { value.startsWith(it) }) {
            throw UnrecognizedEntityVersionException(value)
        }
    }

    override fun getBindAttributes(): Set<Pair<String, String>> {
        return setOf(Pair(ENTITIES_NAMESPACE, "saveto"))
    }

    override fun processBindAttribute(name: String, value: String, binding: DataBinding) {
        val reference = (binding.reference as XPathReference).reference as TreeReference
        saveTos.add(SaveTo(reference, value))
    }

    @Throws(XFormParser.ParseException::class)
    override fun processFormDef(formDef: FormDef) {
        if (isEntityForm(formDef)) {
            version.let {
                if (it == null) {
                    throw MissingModelAttributeException(ENTITIES_NAMESPACE, "entities-version")
                } else if (LOCAL_ENTITY_VERSIONS.any { prefix -> it.startsWith(prefix) }) {
                    for (saveTo in saveTos) {
                        val parentElement = formDef.mainInstance.resolveReference(saveTo.reference).parent as TreeElement
                        findNearestEntityGroupElement(parentElement)?.let { entityGroup ->
                            saveTo.updateEntityReference(entityGroup.ref.genericize())
                        }
                    }
                    val entityFormExtra = EntityFormExtra(saveTos)
                    formDef.extras.put(entityFormExtra)
                }
            }
        }
    }

    private fun findNearestEntityGroupElement(element: TreeElement): TreeElement? {
        var currentElement = element
        while (currentElement != null) {
            if (EntityFormParser.hasEntityElement(currentElement)) {
                return currentElement
            }
            currentElement = currentElement.parent as TreeElement
        }
        return null
    }

    companion object {

        private object VERSIONS {
            const val V2022_1 = "2022.1"
            const val V2023_1 = "2023.1"
            const val V2024_1 = "2024.1"
            const val V2025_1 = "2025.1"
        }

        private const val ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities"
        private val SUPPORTED_VERSIONS = arrayOf(V2022_1, V2023_1, V2024_1, V2025_1)
        private val LOCAL_ENTITY_VERSIONS = arrayOf(V2024_1, V2025_1)

        private fun isEntityForm(formDef: FormDef): Boolean {
            return EntityFormParser.hasEntityElement(formDef.mainInstance.root)
        }
    }
}
