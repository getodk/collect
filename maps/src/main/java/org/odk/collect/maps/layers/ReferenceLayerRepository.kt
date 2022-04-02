package org.odk.collect.maps.layers

import java.io.File

interface ReferenceLayerRepository {

    fun getAll(): List<ReferenceLayer>
    fun get(id: String): ReferenceLayer?
}

data class ReferenceLayer(val id: String, val file: File)
