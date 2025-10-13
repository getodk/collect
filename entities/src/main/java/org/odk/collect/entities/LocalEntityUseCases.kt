package org.odk.collect.entities

import org.apache.commons.csv.CSVRecord
import org.javarosa.core.model.instance.SecondaryInstanceCSVParserBuilder
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.parse.EntitySchema
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.server.EntitySource
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.Query
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
                        val hasValidLabel = !label.isNullOrBlank()
                        val list = entitiesRepository.getList(formEntity.dataset)
                        if (hasValidLabel && list != null && !list.needsApproval) {
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
                        val existing = entitiesRepository.query(
                            formEntity.dataset,
                            Query.StringEq(EntitySchema.ID, formEntity.id)
                        ).firstOrNull()

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
        entitiesRepository: EntitiesRepository,
        entitySource: EntitySource,
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

        handleMissingEntities(
            list,
            missingFromServer.values,
            entitiesRepository,
            entitySource,
            mediaFile.integrityUrl
        )
        entitiesRepository.save(list, *newAndUpdated.toTypedArray())
        entitiesRepository.updateList(
            list,
            mediaFile.hash,
            mediaFile.type == MediaFile.Type.APPROVAL_ENTITY_LIST
        )
    }

    fun updateOfflineLocalEntitiesFromServer(
        list: String,
        entitiesRepository: EntitiesRepository,
        entitySource: EntitySource,
        mediaFile: MediaFile
    ) {
        val offlineLocalEntities = entitiesRepository
            .query(list)
            .filter { it.state == Entity.State.OFFLINE }
            .associateBy { it.id }
            .toMutableMap()

        handleMissingEntities(
            list,
            offlineLocalEntities.values,
            entitiesRepository,
            entitySource,
            mediaFile.integrityUrl
        )
    }

    private fun handleMissingEntities(
        list: String,
        missingFromServer: Collection<Entity.Saved>,
        entitiesRepository: EntitiesRepository,
        entitySource: EntitySource,
        integrityUrl: String?
    ) {
        val missingOnline = missingFromServer.filter { it.state == Entity.State.ONLINE }
        val missingOffline = missingFromServer.filter { it.state == Entity.State.OFFLINE }

        missingOnline.forEach {
            entitiesRepository.delete(list, it.id)
        }

        if (integrityUrl != null && missingOffline.isNotEmpty()) {
            entitySource.fetchDeletedStates(integrityUrl, missingOffline.map { it.id }).forEach {
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
