package org.odk.collect.maps.layers

import java.io.File

class TestReferenceLayerRepository : ReferenceLayerRepository {
    private val layers = mutableListOf<ReferenceLayer>()

    override fun getAll(): List<ReferenceLayer> {
        return layers
    }

    override fun get(id: String): ReferenceLayer? {
        return layers.find { it.id == id }
    }

    override fun addLayer(file: File, shared: Boolean) {
        TODO("Not yet implemented")
    }

    fun addLayers(vararg newLayers: ReferenceLayer) {
        newLayers.forEach { newLayer ->
            layers.add(newLayer)
        }
    }
}
