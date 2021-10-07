package org.odk.collect.android.geo

import org.odk.collect.shared.PathUtils
import org.odk.collect.shared.files.DirectoryUtils.listFilesRecursively
import java.io.File

class DirectoryReferenceLayerRepository(private val directoryPaths: List<String>) :
    ReferenceLayerRepository {

    /**
     * Convenience constructors
     */
    constructor(vararg directoryPaths: String) : this(directoryPaths.toList())
    constructor(directoryPath: String) : this(listOf(directoryPath))

    override fun getAll(): List<ReferenceLayer> {
        return getAllFilesWithDirectory().map {
            ReferenceLayer(getIdForFile(it.second, it.first), it.first)
        }.distinctBy { it.id }
    }

    override fun get(id: String): ReferenceLayer? {
        val file = getAllFilesWithDirectory().firstOrNull { getIdForFile(it.second, it.first) == id }

        return if (file != null) {
            ReferenceLayer(getIdForFile(file.second, file.first), file.first)
        } else {
            null
        }
    }

    private fun getAllFilesWithDirectory() = directoryPaths.flatMap { dir ->
        listFilesRecursively(File(dir)).map { file ->
            Pair(file, dir)
        }
    }

    private fun getIdForFile(directoryPath: String, file: File) =
        PathUtils.getRelativeFilePath(directoryPath, file.absolutePath)
}
