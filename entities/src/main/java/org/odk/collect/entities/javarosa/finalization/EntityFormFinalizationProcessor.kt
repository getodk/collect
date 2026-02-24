package org.odk.collect.entities.javarosa.finalization

import org.javarosa.core.model.instance.FormInstance
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.form.api.FormEntryFinalizationProcessor
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.entities.javarosa.parse.EntityFormExtra
import org.odk.collect.entities.javarosa.parse.SaveTo
import org.odk.collect.entities.javarosa.parse.StringExt.isV4UUID
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.javarosa.spec.EntityFormParser

class EntityFormFinalizationProcessor : FormEntryFinalizationProcessor {
    override fun processForm(formEntryModel: FormEntryModel) {
        val formDef = formEntryModel.form
        val mainInstance = formDef.mainInstance

        val entityFormExtra = formDef.extras.get(EntityFormExtra::class.java)
        if (entityFormExtra != null) {
            val saveTos = entityFormExtra.saveTos

            val entityElements = EntityFormParser.getEntityElements(mainInstance.getRoot())
            val entitiesExtra = entityElements.fold(EntitiesExtra()) { extra, element ->
                val action = EntityFormParser.parseAction(element)
                val dataset = EntityFormParser.parseDataset(element)!!
                val id = EntityFormParser.parseId(element)
                val label = EntityFormParser.parseLabel(element)

                if (action == EntityAction.CREATE || action == EntityAction.UPDATE) {
                    val entity = createEntity(
                        dataset,
                        EntityFormParser.parseId(element),
                        EntityFormParser.parseLabel(element),
                        element.ref,
                        saveTos,
                        action,
                        mainInstance
                    )

                    if (entity != null) {
                        extra.copy(entities = extra.entities + entity)
                    } else {
                        val invalidEntity = InvalidEntity(dataset, id, label)
                        extra.copy(invalidEntities = extra.invalidEntities + invalidEntity)
                    }
                } else {
                    extra
                }
            }

            formEntryModel.extras.put(entitiesExtra)
        }
    }

    private fun createEntity(
        dataset: String,
        id: String?,
        label: String?,
        elementRef: TreeReference,
        saveTos: List<SaveTo>,
        action: EntityAction,
        mainInstance: FormInstance
    ): FormEntity? {
        val entityGroupRef = elementRef.getParentRef().getParentRef()
        val fields = saveTos.mapNotNull { saveTo ->
            if (!entityGroupRef.genericize().equals(saveTo.entityGroupReference)) {
                null
            } else {
                val entityBindRef = saveTo.reference
                val entityFieldRef = entityBindRef.contextualize(entityGroupRef)

                val element = mainInstance.resolveReference(entityFieldRef)
                if (element.isRelevant) {
                    val value = element.value?.uncast()?.string ?: ""
                    saveTo.value to value
                } else {
                    null
                }
            }
        }

        return if (id.isV4UUID() && !label.isNullOrBlank()) {
            FormEntity(action, dataset, id, label, fields)
        } else {
            null
        }
    }
}
