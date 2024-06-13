package org.odk.collect.maps.layers

import java.io.File

interface ReferenceLayerRepository {

    fun getAll(): List<ReferenceLayer>
    fun get(id: String): ReferenceLayer?
    fun addLayer(file: File, shared: Boolean)
    fun delete(id: String)
}

data class ReferenceLayer(val id: String, val file: File, val name: String)
