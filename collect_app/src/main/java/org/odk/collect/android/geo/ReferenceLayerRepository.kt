package org.odk.collect.android.geo

import java.io.File

interface ReferenceLayerRepository {

    fun getAll(): List<ReferenceLayer>
    fun get(id: String): ReferenceLayer?
}

data class ReferenceLayer(val id: String, val file: File)
