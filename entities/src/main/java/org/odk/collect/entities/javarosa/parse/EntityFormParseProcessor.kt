package org.odk.collect.entities.javarosa.parse

import org.javarosa.core.model.DataBinding
import org.javarosa.core.model.FormDef
import org.javarosa.model.xform.XPathReference
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParser.BindAttributeProcessor
import org.javarosa.xform.parse.XFormParser.FormDefProcessor
import org.javarosa.xform.parse.XFormParser.MissingModelAttributeException
import org.javarosa.xform.parse.XFormParser.ModelAttributeProcessor
import org.odk.collect.entities.BuildConfig
import org.odk.collect.entities.javarosa.spec.EntityFormParser
import org.odk.collect.entities.javarosa.spec.UnrecognizedEntityVersionException
import java.util.stream.Stream

class EntityFormParseProcessor : BindAttributeProcessor, FormDefProcessor, ModelAttributeProcessor {
    private val saveTos: MutableList<Pair<XPathReference?, String?>?> =
        ArrayList<Pair<XPathReference?, String?>?>()
    private var version: String? = null

    override fun getModelAttributes(): MutableSet<Pair<String?, String?>?> {
        val attributes = HashSet<Pair<String?, String?>?>()
        attributes.add(Pair<String?, String?>(ENTITIES_NAMESPACE, "entities-version"))

        return attributes
    }

    @Throws(XFormParser.ParseException::class)
    override fun processModelAttribute(name: String?, value: String) {
        version = value

        if (BuildConfig.DEBUG && value.startsWith("v2025.1")) {
            return
        }

        if (Stream.of<String?>(*SUPPORTED_VERSIONS).noneMatch { prefix: String? ->
                value.startsWith(
                    prefix!!
                )
            }) {
            throw UnrecognizedEntityVersionException(version!!)
        }
    }

    override fun getBindAttributes(): Set<Pair<String, String>> {
        return setOf(Pair(ENTITIES_NAMESPACE, "saveto"))
    }

    override fun processBindAttribute(name: String, value: String, binding: DataBinding) {
        saveTos.add(Pair(binding.reference as XPathReference, value))
    }

    @Throws(XFormParser.ParseException::class)
    override fun processFormDef(formDef: FormDef) {
        if (isEntityForm(formDef)) {
            if (version == null) {
                throw MissingModelAttributeException(ENTITIES_NAMESPACE, "entities-version")
            } else if (Stream.of<String?>(*LOCAL_ENTITY_VERSIONS).anyMatch { prefix: String? ->
                    version!!.startsWith(
                        prefix!!
                    )
                }) {
                val entityFormExtra = EntityFormExtra(saveTos)
                formDef.getExtras().put(entityFormExtra)
            }
        }
    }

    companion object {
        private const val ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities"
        private val SUPPORTED_VERSIONS = arrayOf<String?>("2022.1", "2023.1", "2024.1")
        private val LOCAL_ENTITY_VERSIONS = arrayOf<String?>("2024.1")

        private fun isEntityForm(formDef: FormDef): Boolean {
            return EntityFormParser.getEntityElement(formDef.getMainInstance()) != null
        }
    }
}
