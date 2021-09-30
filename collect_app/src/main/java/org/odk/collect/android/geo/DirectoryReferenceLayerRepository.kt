package org.odk.collect.android.geo

import org.odk.collect.shared.files.DirectoryUtils.listFilesRecursively
import java.io.File

class DirectoryReferenceLayerRepository(private val directoryPath: String) :
    ReferenceLayerRepository {

    override fun getAll(): List<File> {
        return listFilesRecursively(File(directoryPath))
    }
}
