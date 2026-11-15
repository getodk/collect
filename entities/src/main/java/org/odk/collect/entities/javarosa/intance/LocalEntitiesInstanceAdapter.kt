package org.odk.collect.entities.javarosa.intance

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.odk.collect.entities.javarosa.parse.EntitySchema
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.getListNames
import org.odk.collect.forms.FormMediaFileRepository
import org.odk.collect.shared.Query

class LocalEntitiesInstanceAdapter(
    private val entitiesRepository: EntitiesRepository,
    private val mediaFileRepository: FormMediaFileRepository
) {

    private val lists = entitiesRepository.getListNames()

    /**
     * Entity lists are stored in the database, not as files on disk (their seed CSV is deleted
     * after import). So if the form has a CSV attached with the same name as an entity list, it
     * must be a plain attached CSV, which should be used instead of the entity list.
     */
    fun supportsInstance(instanceId: String): Boolean {
        return lists.contains(instanceId) &&
            mediaFileRepository.get("jr://file-csv/$instanceId.csv")?.exists() != true
    }

    fun getAll(instanceId: String, partial: Boolean): List<TreeElement> {
        return if (partial) {
            val count = entitiesRepository.getCount(instanceId)

            if (count > 0) {
                val first = entitiesRepository.getByIndex(instanceId, 0)!!

                0.until(count).map {
                    if (it == 0) {
                        convertToElement(first)
                    } else {
                        TreeElement("item", it, true)
                    }
                }
            } else {
                emptyList()
            }
        } else {
            entitiesRepository.query(instanceId).map { entity ->
                convertToElement(entity)
            }
        }
    }

    fun query(list: String, query: Query): List<TreeElement> {
        return entitiesRepository
            .query(list, query)
            .map { convertToElement(it) }
    }

    private fun convertToElement(entity: Entity.Saved): TreeElement {
        val name = TreeElement(EntitySchema.ID)
        val label = TreeElement(EntitySchema.LABEL)
        val version = TreeElement(EntitySchema.VERSION)
        val trunkVersion = TreeElement(EntitySchema.TRUNK_VERSION)
        val branchId = TreeElement(EntitySchema.BRANCH_ID)

        name.value = StringData(entity.id)
        version.value = StringData(entity.version.toString())
        branchId.value = StringData(entity.branchId)

        if (entity.label != null) {
            label.value = StringData(entity.label)
        }

        if (entity.trunkVersion != null) {
            trunkVersion.value = StringData(entity.trunkVersion.toString())
        }

        val item = TreeElement("item", entity.index, false)
        item.addChild(name)
        item.addChild(label)
        item.addChild(version)
        item.addChild(trunkVersion)
        item.addChild(branchId)

        entity.properties.forEach { property ->
            val propertyElement = TreeElement(property.first)
            propertyElement.value = StringData(property.second)
            item.addChild(propertyElement)
        }

        return item
    }
}
