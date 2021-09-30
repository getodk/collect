package org.odk.collect.android.geo

import java.io.File

interface ReferenceLayerRepository {

    fun getAll(): List<File>
}
