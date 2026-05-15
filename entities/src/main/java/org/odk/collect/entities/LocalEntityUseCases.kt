package org.odk.collect.entities

import org.apache.commons.csv.CSVRecord
import org.javarosa.core.model.instance.SecondaryInstanceCSVParserBuilder
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.finalization.FormEntity
import org.odk.collect.entities.javarosa.parse.EntitySchema
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.server.EntitySource
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.findEntityById
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.DebugLogger
import java.io.File
import java.util.UUID

object LocalEntityUseCases {

    @JvmStatic
    fun updateLocalEntitiesFromForm(
        formEntities: EntitiesExtra?,
        entitiesRepository: EntitiesRepository,
        debugLogger: DebugLogger? = null
    ) {
        formEntities?.entities?.forEach { formEntity ->
            when (formEntity.action) {
                EntityAction.CREATE -> saveNewEntity(formEntity, entitiesRepository, debugLogger)

                EntityAction.UPDATE -> {
                    val existing = entitiesRepository.findEntityById(formEntity.dataset, formEntity.id)
                    if (existing != null) {
                        saveUpdatedEntity(formEntity, existing, entitiesRepository)
                    }
                }

                EntityAction.UPSERT -> {
                    val existing = entitiesRepository.findEntityById(formEntity.dataset, formEntity.id)
                    if (existing == null) {
                        saveNewEntity(formEntity, entitiesRepository, debugLogger)
                    } else {
                        saveUpdatedEntity(formEntity, existing, entitiesRepository)
                    }
                }
            }
        }

        formEntities?.invalidEntities?.forEach {
            debugLogger?.log(
                "Entities",
                "Failed to create/update dataset=${it.dataset}, id=${it.id}, label=${it.label}"
            )
        }
    }

    private fun saveNewEntity(
        formEntity: FormEntity,
        entitiesRepository: EntitiesRepository,
        debugLogger: DebugLogger? = null
    ) {
        if (formEntity.label.isNotBlank()) {
            val list = entitiesRepository.getList(formEntity.dataset)
            if (list != null && !list.needsApproval) {
                entitiesRepository.save(
                    formEntity.dataset,
                    Entity.New(
                        formEntity.id,
                        formEntity.label,
                        1,
                        formEntity.properties,
                        branchId = UUID.randomUUID().toString()
                    )
                )
            }
        } else {
            debugLogger?.log(
                "Entities",
                "Failed to create dataset=${formEntity.dataset}, id=${formEntity.id}, label=${formEntity.label}"
            )
        }
    }

    private fun saveUpdatedEntity(
        formEntity: FormEntity,
        existing: Entity.Saved,
        entitiesRepository: EntitiesRepository
    ) {
        entitiesRepository.save(
            formEntity.dataset,
            existing.copy(
                label = formEntity.label.ifBlank { existing.label },
                properties = formEntity.properties,
                version = existing.version + 1
            )
        )
    }

    fun updateLocalEntitiesFromServer(
        list: String,
        serverList: File,
        entitiesRepository: EntitiesRepository,
        mediaFile: MediaFile
    ) {
        val existingListHash = entitiesRepository.getList(list)?.hash
        if (mediaFile.hash == existingListHash) {
            return
        }

        val csvParser = try {
            SecondaryInstanceCSVParserBuilder()
                .path(serverList.absolutePath)
                .build()
        } catch (_: Exception) {
            return
        }

        val serverProperties = csvParser.headerMap.removeReservedProperties().keys
        entitiesRepository.cleanUpProperties(list, serverProperties)
        val localEntities = entitiesRepository.query(list)

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

        missingFromServer.values
            .filter { it.state == Entity.State.ONLINE }
            .forEach {
                entitiesRepository.delete(list, it.id)
            }

        entitiesRepository.save(list, *newAndUpdated.toTypedArray())
        entitiesRepository.updateList(
            list,
            mediaFile.hash,
            mediaFile.type == MediaFile.Type.APPROVAL_ENTITY_LIST
        )
    }

    fun cleanUpDeletedOfflineEntities(
        list: String,
        entitiesRepository: EntitiesRepository,
        entitySource: EntitySource,
        mediaFile: MediaFile
    ) {
        val offlineLocalEntities = entitiesRepository
            .query(list)
            .filter { it.state == Entity.State.OFFLINE }

        val integrityUrl = mediaFile.integrityUrl
        if (integrityUrl != null && offlineLocalEntities.isNotEmpty()) {
            entitySource.fetchDeletedStates(integrityUrl, offlineLocalEntities.map { it.id })
                .forEach {
                    if (it.second) {
                        entitiesRepository.delete(list, it.first)
                    }
                }
        }
    }

    private fun parseEntityFromRecord(record: CSVRecord): ServerEntity? {
        val map = record.toMap()

        val id = map[EntitySchema.ID]
        val label = map[EntitySchema.LABEL]
        val version = map[EntitySchema.VERSION]?.toInt()
        if (id == null || label == null || version == null) {
            return null
        }

        return ServerEntity(
            id,
            label,
            version,
            map.removeReservedProperties()
        )
    }
}

private data class ServerEntity(
    val id: String,
    val label: String,
    val version: Int,
    val properties: Map<String, String>
) {

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

private fun <T> Map<String, T>.removeReservedProperties(): Map<String, T> {
    return filterNot {
        it.key == EntitySchema.ID || it.key == EntitySchema.LABEL || it.key.startsWith("__")
    }
}
