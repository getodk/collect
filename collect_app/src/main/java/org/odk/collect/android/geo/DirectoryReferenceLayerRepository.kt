package org.odk.collect.android.geo

import org.odk.collect.shared.PathUtils
import org.odk.collect.shared.files.DirectoryUtils.listFilesRecursively
import java.io.File

class DirectoryReferenceLayerRepository(private val directoryPath: String) :
    ReferenceLayerRepository {

    override fun getAll(): List<ReferenceLayer> {
        return listFilesRecursively(File(directoryPath)).map {
            ReferenceLayer(
                getIdForFile(it),
                it
            )
        }
    }

    override fun get(id: String): ReferenceLayer? {
        val file = listFilesRecursively(File(directoryPath)).firstOrNull {
            id == getIdForFile(it)
        }

        return if (file != null) {
            ReferenceLayer(getIdForFile(file), file)
        } else {
            null
        }
    }

    private fun getIdForFile(it: File) =
        PathUtils.getRelativeFilePath(directoryPath, it.absolutePath)
}
