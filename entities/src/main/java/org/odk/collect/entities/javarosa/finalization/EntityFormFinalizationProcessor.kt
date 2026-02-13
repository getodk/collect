package org.odk.collect.entities.javarosa.finalization

import org.javarosa.core.model.instance.FormInstance
import org.javarosa.core.model.instance.TreeElement
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
            val entitiesExtra =
                entityElements.fold(EntitiesExtra()) { entitiesExtra, entityElement ->
                    val action = EntityFormParser.parseAction(entityElement)
                    val dataset = EntityFormParser.parseDataset(entityElement)!!

                    if (action == EntityAction.CREATE || action == EntityAction.UPDATE) {
                        val entity =
                            createEntity(entityElement, dataset, saveTos, mainInstance, action)
                        if (entity != null) {
                            entitiesExtra.copy(entities = entitiesExtra.entities + entity)
                        } else {
                            val invalidEntity = InvalidEntity(
                                dataset,
                                EntityFormParser.parseId(entityElement),
                                EntityFormParser.parseLabel(entityElement)
                            )
                            entitiesExtra.copy(invalidEntities = entitiesExtra.invalidEntities + invalidEntity)
                        }
                    } else {
                        entitiesExtra
                    }
                }

            formEntryModel.extras.put(entitiesExtra)
        }
    }

    private fun createEntity(
        entityElement: TreeElement,
        dataset: String,
        saveTos: List<SaveTo>,
        mainInstance: FormInstance,
        action: EntityAction
    ): FormEntity? {
        val entityGroupRef = entityElement.ref.getParentRef().getParentRef()
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

        val id = EntityFormParser.parseId(entityElement)
        val label = EntityFormParser.parseLabel(entityElement)

        return if (id.isV4UUID() && !label.isNullOrBlank()) {
            FormEntity(action, dataset, id, label, fields)
        } else {
            null
        }
    }
}
