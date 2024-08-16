package org.odk.collect.entities

import org.apache.commons.csv.CSVRecord
import org.javarosa.core.model.instance.SecondaryInstanceCSVParserBuilder
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.parse.EntityItemElement
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import java.io.File

object LocalEntityUseCases {

    @JvmStatic
    fun updateLocalEntitiesFromForm(
        formEntities: EntitiesExtra?,
        entitiesRepository: EntitiesRepository
    ) {
        formEntities?.entities?.forEach { formEntity ->
            val id = formEntity.id
            if (id != null) {
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
        val csvParser = try {
            SecondaryInstanceCSVParserBuilder()
                .path(serverList.absolutePath)
                .build()
        } catch (_: Exception) {
            return
        }

        val localEntities = entitiesRepository.getEntities(list)

        val missingFromServer = localEntities.associateBy { it.id }.toMutableMap()
        val newAndUpdated = ArrayList<Entity>()
        csvParser.use {
            it.forEach { record ->
                val serverEntity = parseEntityFromRecord(record, list) ?: return
                val existing = missingFromServer.remove(serverEntity.id)

                if (existing == null || existing.version <= serverEntity.version) {
                    newAndUpdated.add(serverEntity)
                } else if (existing.state == Entity.State.OFFLINE) {
                    val update = existing.copy(state = Entity.State.ONLINE)
                    newAndUpdated.add(update)
                }
            }
        }

        missingFromServer.values.forEach {
            if (it.state == Entity.State.ONLINE) {
                entitiesRepository.delete(it.id)
            }
        }

        entitiesRepository.save(*newAndUpdated.toTypedArray())
    }

    private fun parseEntityFromRecord(
        record: CSVRecord,
        list: String
    ): Entity? {
        val map = record.toMap().toMutableMap()

        val id = map.remove(EntityItemElement.ID)
        val label = map.remove(EntityItemElement.LABEL)
        val version = map.remove(EntityItemElement.VERSION)?.toInt()
        if (id == null || label == null || version == null) {
            return null
        }

        val properties = map.entries.fold(emptyList<Pair<String, String>>()) { properties, entry ->
            properties + Pair(entry.key, entry.value)
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
