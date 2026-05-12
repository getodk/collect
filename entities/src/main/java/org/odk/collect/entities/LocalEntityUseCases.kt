package org.odk.collect.entities

import org.apache.commons.csv.CSVRecord
import org.javarosa.core.model.instance.SecondaryInstanceCSVParserBuilder
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
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
            val id = formEntity.id
            val label = formEntity.label
            when (formEntity.action) {
                EntityAction.CREATE -> saveNewEntity(id, label, formEntity.dataset, formEntity.properties, entitiesRepository, debugLogger)

                EntityAction.UPDATE -> {
                    val existing = entitiesRepository.findEntityById(formEntity.dataset, id)
                    if (existing != null) {
                        saveUpdatedEntity(label, formEntity.dataset, formEntity.properties, existing, entitiesRepository)
                    }
                }

                EntityAction.UPSERT -> {
                    val existing = entitiesRepository.findEntityById(formEntity.dataset, id)
                    if (existing == null) {
                        saveNewEntity(id, label, formEntity.dataset, formEntity.properties, entitiesRepository, debugLogger)
                    } else {
                        saveUpdatedEntity(label, formEntity.dataset, formEntity.properties, existing, entitiesRepository)
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
        id: String,
        label: String,
        dataset: String,
        properties: List<Pair<String, String>>,
        entitiesRepository: EntitiesRepository,
        debugLogger: DebugLogger? = null
    ) {
        if (label.isNotBlank()) {
            val list = entitiesRepository.getList(dataset)
            if (list != null && !list.needsApproval) {
                entitiesRepository.save(
                    dataset,
                    Entity.New(
                        id,
                        label,
                        1,
                        properties,
                        branchId = UUID.randomUUID().toString()
                    )
                )
            }
        } else {
            debugLogger?.log(
                "Entities",
                "Failed to create dataset=$dataset, id=$id, label=$label"
            )
        }
    }

    private fun saveUpdatedEntity(
        label: String,
        dataset: String,
        properties: List<Pair<String, String>>,
        existing: Entity.Saved,
        entitiesRepository: EntitiesRepository
    ) {
        entitiesRepository.save(
            dataset,
            existing.copy(
                label = label.ifBlank { existing.label },
                properties = properties,
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

        val id = map.remove(EntitySchema.ID)
        val label = map.remove(EntitySchema.LABEL)
        val version = map.remove(EntitySchema.VERSION)?.toInt()
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
