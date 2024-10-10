package org.odk.collect.maps.layers

import java.io.File

class InMemReferenceLayerRepository : ReferenceLayerRepository {
    val sharedLayers = mutableListOf<ReferenceLayer>()
    val projectLayers = mutableListOf<ReferenceLayer>()

    override fun getAll(): List<ReferenceLayer> {
        return sharedLayers + projectLayers
    }

    override fun get(id: String): ReferenceLayer? {
        return sharedLayers.find { it.id == id } ?: projectLayers.find { it.id == id }
    }

    override fun addLayer(file: File, shared: Boolean) {
        if (shared) {
            sharedLayers.add(ReferenceLayer(file.absolutePath, file, file.name))
        } else {
            projectLayers.add(ReferenceLayer(file.absolutePath, file, file.name))
        }
    }

    override fun delete(id: String) {
        sharedLayers.removeIf { it.id == id }
        projectLayers.removeIf { it.id == id }
    }
}
