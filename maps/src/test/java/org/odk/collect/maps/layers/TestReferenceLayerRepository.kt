package org.odk.collect.maps.layers

import org.odk.collect.shared.TempFiles

class TestReferenceLayerRepository : ReferenceLayerRepository {
    private val sharedLayersDirPath: String = TempFiles.createTempDir().absolutePath
    private val projectLayersDirPath: String = TempFiles.createTempDir().absolutePath

    private val layers = mutableListOf<ReferenceLayer>()

    override fun getAll(): List<ReferenceLayer> {
        return layers
    }

    override fun get(id: String): ReferenceLayer? {
        return layers.find { it.id == id }
    }

    override fun getSharedLayersDirPath(): String {
        return sharedLayersDirPath
    }

    override fun getProjectLayersDirPath(): String {
        return projectLayersDirPath
    }

    fun addLayers(vararg newLayers: ReferenceLayer) {
        newLayers.forEach { newLayer ->
            layers.add(newLayer)
        }
    }
}
