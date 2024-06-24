package org.odk.collect.maps.layers

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.settings.Settings
import java.io.File

class DirectoryReferenceLayerRepositoryTest {
    private val sharedLayersDir = TempFiles.createTempDir()
    private val projectLayersDir = TempFiles.createTempDir()
    private var mapConfigurator = StubMapConfigurator()
    private val repository = DirectoryReferenceLayerRepository(
        sharedLayersDir.absolutePath,
        projectLayersDir.absolutePath
    ) { mapConfigurator }

    @Test
    fun getAll_returnsAllSupportedLayersInTheDirectory() {
        val file1 = TempFiles.createTempFile(sharedLayersDir)
        val file2 = TempFiles.createTempFile(sharedLayersDir)
        val file3 = TempFiles.createTempFile(sharedLayersDir)
        mapConfigurator.apply {
            addFile(file1, true, file1.name)
            addFile(file2, false, file2.name)
            addFile(file3, true, file3.name)
        }

        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAll_returnsAllSupportedLayersInSubDirectories() {
        val subDir = TempFiles.createTempDir(sharedLayersDir)
        val file1 = TempFiles.createTempFile(subDir)
        val file2 = TempFiles.createTempFile(subDir)
        val file3 = TempFiles.createTempFile(subDir)
        mapConfigurator.apply {
            addFile(file1, true, file1.name)
            addFile(file2, false, file2.name)
            addFile(file3, true, file3.name)
        }

        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAll_withMultipleDirectories_returnsAllSupportedLayersInAllDirectories() {
        val file1 = TempFiles.createTempFile(sharedLayersDir)
        val file2 = TempFiles.createTempFile(sharedLayersDir)
        val file3 = TempFiles.createTempFile(projectLayersDir)
        val file4 = TempFiles.createTempFile(projectLayersDir)
        mapConfigurator.apply {
            addFile(file1, true, file1.name)
            addFile(file2, false, file2.name)
            addFile(file3, true, file3.name)
            addFile(file4, false, file4.name)
        }

        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file3))
    }

    /**
     * We do this so we don't end up returns a list of reference layers with non-unique IDs. If two
     * (or more) files have the same relative path, only the first one (in the order of declared
     * layer directories) will be returned.
     */
    @Test
    fun getAll_withMultipleDirectoriesWithFilesWithTheSameRelativePath_onlyReturnsTheSupportedFileFromTheFirstDirectory() {
        val file1 = TempFiles.createTempFile(sharedLayersDir, "blah", ".temp")
        val file2 = TempFiles.createTempFile(projectLayersDir, "blah", ".temp")
        mapConfigurator.apply {
            addFile(file1, true, file1.name)
            addFile(file2, true, file2.name)
        }

        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1))
    }

    @Test
    fun getAllAlwaysUsesMapConfiguratorThatRepresentsTheCurrentConfiguration() {
        val file = TempFiles.createTempFile(sharedLayersDir)

        mapConfigurator.apply {
            addFile(file, false, file.name)
        }

        assertThat(repository.getAll().isEmpty(), equalTo(true))

        mapConfigurator = StubMapConfigurator().apply {
            addFile(file, true, file.name)
        }

        assertThat(repository.getAll().isEmpty(), equalTo(false))
    }

    @Test
    fun get_returnsProperLayer() {
        val file1 = TempFiles.createTempFile(sharedLayersDir)
        val file2 = TempFiles.createTempFile(sharedLayersDir)
        mapConfigurator.apply {
            addFile(file1, true, file1.name)
            addFile(file2, true, file2.name)
        }

        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_returnsNullIfLayerIsNotSupported() {
        val file = TempFiles.createTempFile(sharedLayersDir)
        mapConfigurator.apply {
            addFile(file, false, file.name)
        }

        val fileId = repository.getIdForFile(sharedLayersDir.absolutePath, file)
        assertThat(repository.get(fileId), equalTo(null))
    }

    @Test
    fun get_withMultipleDirectories_returnsLayer() {
        val file1 = TempFiles.createTempFile(sharedLayersDir)
        val file2 = TempFiles.createTempFile(projectLayersDir)
        mapConfigurator.apply {
            addFile(file1, true, file1.name)
            addFile(file2, true, file2.name)
        }

        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectoriesWithFilesWithTheSameRelativePath_returnsLayerFromFirstDirectory() {
        val file1 = TempFiles.createTempFile(sharedLayersDir, "blah", ".temp")
        val file2 = TempFiles.createTempFile(projectLayersDir, "blah", ".temp")
        mapConfigurator.apply {
            addFile(file1, true, file1.name)
            addFile(file2, true, file2.name)
        }

        val layerId = repository.getAll().first().id
        assertThat(repository.get(layerId)!!.file, equalTo(file1))
    }

    @Test
    fun get_whenFileDoesNotExist_returnsNull() {
        val file = TempFiles.createTempFile(sharedLayersDir)
        mapConfigurator.apply {
            addFile(file, true, file.name)
        }

        val fileLayer = repository.getAll().first { it.file == file }

        file.delete()
        assertThat(repository.get(fileLayer.id), equalTo(null))
    }

    @Test
    fun get_returnsLayerWithCorrectName() {
        val file = TempFiles.createTempFile(sharedLayersDir)

        mapConfigurator.apply {
            addFile(file, true, file.name)
        }

        val fileLayer = repository.getAll().first { it.file == file }

        assertThat(repository.get(fileLayer.id)!!.name, equalTo(file.name))
    }

    @Test
    fun getAlwaysUsesMapConfiguratorThatRepresentsTheCurrentConfiguration() {
        val file = TempFiles.createTempFile(sharedLayersDir)

        mapConfigurator.apply {
            addFile(file, false, file.name)
        }

        val fileId = repository.getIdForFile(sharedLayersDir.absolutePath, file)
        assertThat(repository.get(fileId), equalTo(null))

        mapConfigurator = StubMapConfigurator().apply {
            addFile(file, true, file.name)
        }

        assertThat(repository.get(fileId)!!.file, equalTo(file))
    }

    @Test
    fun addLayer_movesFileToTheSharedLayersDir_whenSharedIsTrue() {
        val file = TempFiles.createTempFile().also {
            it.writeText("blah")
        }

        repository.addLayer(file, true)

        assertThat(sharedLayersDir.listFiles().size, equalTo(1))
        assertThat(sharedLayersDir.listFiles()[0].name, equalTo(file.name))
        assertThat(sharedLayersDir.listFiles()[0].readText(), equalTo("blah"))
        assertThat(projectLayersDir.listFiles().size, equalTo(0))
    }

    @Test
    fun addLayer_movesFileToTheProjectLayersDir_whenSharedIsFalse() {
        val file = TempFiles.createTempFile().also {
            it.writeText("blah")
        }

        repository.addLayer(file, false)

        assertThat(sharedLayersDir.listFiles().size, equalTo(0))
        assertThat(projectLayersDir.listFiles().size, equalTo(1))
        assertThat(projectLayersDir.listFiles()[0].name, equalTo(file.name))
        assertThat(projectLayersDir.listFiles()[0].readText(), equalTo("blah"))
    }

    @Test
    fun delete_deletesLayerWithId() {
        val file1 = TempFiles.createTempFile(sharedLayersDir)
        val file2 = TempFiles.createTempFile(sharedLayersDir)

        mapConfigurator.apply {
            addFile(file1, true, file2.name)
            addFile(file2, true, file2.name)
        }

        val fileLayer1 = repository.getAll().first { it.file == file1 }
        val fileLayer2 = repository.getAll().first { it.file == file2 }
        repository.delete(fileLayer1.id)

        assertThat(repository.getAll(), contains(fileLayer2))
    }

    private class StubMapConfigurator : MapConfigurator {
        private val files = mutableMapOf<File, Pair<Boolean, String>>()

        override fun supportsLayer(file: File): Boolean {
            return files[file]!!.first
        }

        override fun getDisplayName(file: File): String {
            return files[file]!!.second
        }

        fun addFile(file: File, isSupported: Boolean, displayName: String) {
            files[file] = Pair(isSupported, displayName)
        }

        override fun isAvailable(context: Context): Boolean {
            TODO("Not yet implemented")
        }

        override fun showUnavailableMessage(context: Context) {
            TODO("Not yet implemented")
        }

        override fun createPrefs(context: Context, settings: Settings): MutableList<Preference> {
            TODO("Not yet implemented")
        }

        override val prefKeys: Collection<String>
            get() = TODO("Not yet implemented")

        override fun buildConfig(prefs: Settings): Bundle {
            TODO("Not yet implemented")
        }
    }
}
