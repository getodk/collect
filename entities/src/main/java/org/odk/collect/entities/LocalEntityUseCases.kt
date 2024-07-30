package org.odk.collect.entities

import org.javarosa.core.model.instance.CsvExternalInstance
import org.javarosa.core.model.instance.TreeElement
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.parse.EntityItemElement
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import timber.log.Timber
import java.io.File

object LocalEntityUseCases {

    @JvmStatic
    fun updateLocalEntitiesFromForm(
        formEntities: EntitiesExtra?,
        entitiesRepository: EntitiesRepository
    ) {
        formEntities?.entities?.forEach { formEntity ->
            val id = formEntity.id
            if (id != null && entitiesRepository.getLists().contains(formEntity.dataset)) {
                when (formEntity.action) {
                    EntityAction.CREATE -> {
                        val entity = Entity.New(
                            formEntity.dataset,
                            id,
                            formEntity.label,
                            1,
                            formEntity.properties
                        )

                        entitiesRepository.save(entity)
                    }

                    EntityAction.UPDATE -> {
                        val existing = entitiesRepository.getById(formEntity.dataset, formEntity.id)
                        if (existing != null) {
                            entitiesRepository.save(
                                existing.copy(
                                    label = formEntity.label,
                                    properties = formEntity.properties,
                                    version = existing.version + 1
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateLocalEntitiesFromServer(
        list: String,
        serverList: File,
        entitiesRepository: EntitiesRepository
    ) {
        Timber.d("SQLite: starting local entity update")

        val root = try {
            CsvExternalInstance().parse(list, serverList.absolutePath)
        } catch (e: Exception) {
            return
        }

        Timber.d("SQLite: processing server entities")
        val localEntities = entitiesRepository.getEntities(list)
        val serverEntities = root.getChildrenWithName("item")

        val missingFromServer = localEntities.associateBy { it.id }.toMutableMap()
        val newAndUpdated = ArrayList<Entity>()
        serverEntities.forEach { item ->
            val serverEntity = parseEntityFromItem(item, list) ?: return
            val existing = missingFromServer.remove(serverEntity.id)

            if (existing == null || existing.version <= serverEntity.version) {
                newAndUpdated.add(serverEntity)
            } else if (existing.state == Entity.State.OFFLINE) {
                val update = existing.copy(state = Entity.State.ONLINE)
                newAndUpdated.add(update)
            }
        }

        Timber.d("SQLite: deleting missing entities from DB")
        missingFromServer.values.forEach {
            if (it.state == Entity.State.ONLINE) {
                entitiesRepository.delete(it.id)
            }
        }

        Timber.d("SQLite: saving entities to DB")
        entitiesRepository.save(*newAndUpdated.toTypedArray())

        Timber.d("SQLite: done updating local entities")
    }

    private fun parseEntityFromItem(
        item: TreeElement,
        list: String
    ): Entity? {
        val id = item.getFirstChild(EntityItemElement.ID)?.value?.value as? String
        val label = item.getFirstChild(EntityItemElement.LABEL)?.value?.value as? String
        val version =
            (item.getFirstChild(EntityItemElement.VERSION)?.value?.value as? String)?.toInt()
        if (id == null || label == null || version == null) {
            return null
        }

        val properties = 0.until(item.numChildren)
            .fold(emptyList<Pair<String, String>>()) { properties, index ->
                val child = item.getChildAt(index)

                if (!listOf(
                        EntityItemElement.ID,
                        EntityItemElement.LABEL,
                        EntityItemElement.VERSION
                    ).contains(child.name)
                ) {
                    properties + Pair(child.name, child.value!!.value as String)
                } else {
                    properties
                }
            }

        return Entity.New(
            list,
            id,
            label,
            version,
            properties,
            state = Entity.State.ONLINE,
            trunkVersion = version
        )
    }
}
