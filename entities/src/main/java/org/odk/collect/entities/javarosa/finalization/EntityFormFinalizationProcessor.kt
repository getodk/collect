package org.odk.collect.entities.javarosa.finalization

import org.javarosa.core.model.instance.FormInstance
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.form.api.FormEntryFinalizationProcessor
import org.javarosa.form.api.FormEntryModel
import org.javarosa.model.xform.XPathReference
import org.odk.collect.entities.javarosa.parse.EntityFormExtra
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
            val entities = mutableListOf<FormEntity>()
            for (entityElement in entityElements) {
                val action = EntityFormParser.parseAction(entityElement)
                val dataset = EntityFormParser.parseDataset(entityElement)

                if (action == EntityAction.CREATE || action == EntityAction.UPDATE) {
                    val entity = createEntity(entityElement, dataset, saveTos, mainInstance, action)
                    entities.add(entity)
                }
            }
            formEntryModel.extras.put(EntitiesExtra(entities))
        }
    }

    private fun createEntity(
        entityElement: TreeElement,
        dataset: String,
        saveTos: MutableList<Pair<XPathReference, String>>,
        mainInstance: FormInstance,
        action: EntityAction
    ): FormEntity {
        val fields = mutableListOf<Pair<String, String>>()
        for (saveTo in saveTos) {
            val entityBindRef = saveTo.first.reference as TreeReference
            val entityGroupRef = entityElement.ref.getParentRef().getParentRef()
            val entityFieldRef = entityBindRef.contextualize(entityGroupRef)

            val element = mainInstance.resolveReference(entityFieldRef)
            if (element.isRelevant) {
                val answerData = element.value
                if (answerData != null) {
                    fields.add(
                        Pair<String, String>(
                            saveTo.second,
                            answerData.uncast().string
                        )
                    )
                } else {
                    fields.add(Pair(saveTo.second, ""))
                }
            }
        }

        val id = EntityFormParser.parseId(entityElement)
        val label = EntityFormParser.parseLabel(entityElement)
        return FormEntity(action, dataset, id, label, fields)
    }
}
