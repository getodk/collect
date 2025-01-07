package org.odk.collect.entities

import org.apache.commons.csv.CSVRecord
import org.javarosa.core.model.instance.SecondaryInstanceCSVParserBuilder
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.parse.EntityItemElement
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.File
import java.util.UUID

object LocalEntityUseCases {

    @JvmStatic
    fun updateLocalEntitiesFromForm(
        formEntities: EntitiesExtra?,
        entitiesRepository: EntitiesRepository
    ) {
        formEntities?.entities?.forEach { formEntity ->
            val id = formEntity.id
            val label = formEntity.label
            if (id != null) {
                when (formEntity.action) {
                    EntityAction.CREATE -> {
                        if (!label.isNullOrBlank()) {
                            val entity = Entity.New(
                                id,
                                label,
                                1,
                                formEntity.properties,
                                branchId = UUID.randomUUID().toString()
                            )

                            entitiesRepository.save(formEntity.dataset, entity)
                        }
                    }

                    EntityAction.UPDATE -> {
                        val existing = entitiesRepository.getById(formEntity.dataset, formEntity.id)
                        if (existing != null) {
                            entitiesRepository.save(
                                formEntity.dataset,
                                existing.copy(
                                    label = if (label.isNullOrBlank()) existing.label else label,
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
        val listHash = getListHash(serverList)
        val existingListVersion = entitiesRepository.getListHash(list)
        if (listHash == existingListVersion) {
            return
        }

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
                val serverEntity = parseEntityFromRecord(record) ?: return
                val existing = missingFromServer.remove(serverEntity.id)

                if (existing == null) {
                    newAndUpdated.add(
                        Entity.New(
                            serverEntity.id,
                            serverEntity.label,
                            serverEntity.version,
                            serverEntity.properties.toList(),
                            state = Entity.State.ONLINE,
                            trunkVersion = serverEntity.version,
                            branchId = UUID.randomUUID().toString()
                        )
                    )
                } else if (existing.version < serverEntity.version) {
                    newAndUpdated.add(serverEntity.updateLocal(existing))
                } else if (existing.version == serverEntity.version) {
                    if (existing.isDirty()) {
                        newAndUpdated.add(serverEntity.updateLocal(existing))
                    }
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

        entitiesRepository.save(list, *newAndUpdated.toTypedArray())
        entitiesRepository.updateListHash(list, listHash)
    }

    private fun getListHash(serverList: File): String {
        return "md5:${serverList.getMd5Hash()!!}"
    }

    private fun parseEntityFromRecord(record: CSVRecord): ServerEntity? {
        val map = record.toMap()

        val id = map.remove(EntityItemElement.ID)
        val label = map.remove(EntityItemElement.LABEL)
        val version = map.remove(EntityItemElement.VERSION)?.toInt()
        if (id == null || label == null || version == null) {
            return null
        }

        return ServerEntity(
            id,
            label,
            version,
            map
        )
    }
}

private data class ServerEntity(
    val id: String,
    val label: String,
    val version: Int,
    val properties: Map<String, String>) {

    fun updateLocal(local: Entity.Saved): Entity.Saved {
        return local.copy(
            label = this.label,
            version = this.version,
            properties = this.properties.toList(),
            state = Entity.State.ONLINE,
            branchId = UUID.randomUUID().toString(),
            trunkVersion = this.version
        )
    }
}
