package org.odk.collect.maps.layers

import org.odk.collect.maps.MapConfigurator
import org.odk.collect.shared.PathUtils
import org.odk.collect.shared.files.DirectoryUtils.listFilesRecursively
import java.io.File

class DirectoryReferenceLayerRepository(
    private val directoryPaths: List<String>,
    private val getMapConfigurator: () -> MapConfigurator
) : ReferenceLayerRepository {

    override fun getAll(): List<ReferenceLayer> {
        return getAllFilesWithDirectory()
            .map { ReferenceLayer(getIdForFile(it.second, it.first), it.first, getName(it.first)) }
            .distinctBy { it.id }
            .filter { getMapConfigurator().supportsLayer(it.file) }
    }

    override fun get(id: String): ReferenceLayer? {
        val file = getAllFilesWithDirectory().firstOrNull { getIdForFile(it.second, it.first) == id }

        return if (file != null) {
            ReferenceLayer(getIdForFile(file.second, file.first), file.first, getName(file.first))
        } else {
            null
        }
    }

    override fun getSharedLayersDirPath(): String {
        return directoryPaths[1]
    }

    override fun getProjectLayersDirPath(): String {
        return directoryPaths[0]
    }

    private fun getAllFilesWithDirectory() = directoryPaths.flatMap { dir ->
        listFilesRecursively(File(dir)).map { file ->
            Pair(file, dir)
        }
    }

    private fun getIdForFile(directoryPath: String, file: File) =
        PathUtils.getRelativeFilePath(directoryPath, file.absolutePath)

    private fun getName(file: File): String {
        return getMapConfigurator().getDisplayName(file)
    }
}
