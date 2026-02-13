package org.odk.collect.entities.javarosa.finalization

import org.javarosa.core.model.instance.FormInstance
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.form.api.FormEntryFinalizationProcessor
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.entities.javarosa.parse.EntityFormExtra
import org.odk.collect.entities.javarosa.parse.SaveTo
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.javarosa.spec.EntityFormParser
import java.util.UUID

class EntityFormFinalizationProcessor : FormEntryFinalizationProcessor {
    override fun processForm(formEntryModel: FormEntryModel) {
        val formDef = formEntryModel.form
        val mainInstance = formDef.mainInstance

        val entityFormExtra = formDef.extras.get(EntityFormExtra::class.java)
        if (entityFormExtra != null) {
            val saveTos = entityFormExtra.saveTos

            val entityElements = EntityFormParser.getEntityElements(mainInstance.getRoot())
            val entities = entityElements.mapNotNull { entityElement ->
                val action = EntityFormParser.parseAction(entityElement)
                val dataset = EntityFormParser.parseDataset(entityElement)!!

                if (action == EntityAction.CREATE || action == EntityAction.UPDATE) {
                    createEntity(entityElement, dataset, saveTos, mainInstance, action)
                } else {
                    null
                }
            }
            formEntryModel.extras.put(EntitiesExtra(entities))
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

        val isIdValid = if (id != null) {
            try {
                UUID.fromString(id) != null
                true
            } catch (_: IllegalArgumentException) {
                false
            }
        } else {
            false
        }

        return if (isIdValid && !label.isNullOrBlank()) {
            FormEntity(action, dataset, id!!, label, fields)
        } else {
            null
        }
    }
}
