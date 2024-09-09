package org.odk.collect.maps.layers

import org.odk.collect.maps.MapConfigurator
import org.odk.collect.shared.PathUtils
import org.odk.collect.shared.files.FileExt.listFilesRecursively
import java.io.File

class DirectoryReferenceLayerRepository(
    private val sharedLayersDirPath: String,
    private val projectLayersDirPath: String,
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

        return if (file != null && getMapConfigurator().supportsLayer(file.first)) {
            ReferenceLayer(getIdForFile(file.second, file.first), file.first, getName(file.first))
        } else {
            null
        }
    }

    override fun addLayer(file: File, shared: Boolean) {
        if (shared) {
            file.copyTo(File(sharedLayersDirPath, file.name), true)
        } else {
            file.copyTo(File(projectLayersDirPath, file.name), true)
        }
    }

    override fun delete(id: String) {
        get(id)?.file?.delete()
    }

    private fun getAllFilesWithDirectory() = listOf(sharedLayersDirPath, projectLayersDirPath).flatMap { dir ->
        File(dir).listFilesRecursively().map { file ->
            Pair(file, dir)
        }
    }

    fun getIdForFile(directoryPath: String, file: File) =
        PathUtils.getRelativeFilePath(directoryPath, file.absolutePath)

    private fun getName(file: File): String {
        return getMapConfigurator().getDisplayName(file)
    }
}
