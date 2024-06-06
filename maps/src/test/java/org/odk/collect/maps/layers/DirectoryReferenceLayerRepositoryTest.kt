package org.odk.collect.maps.layers

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.settings.Settings
import java.io.File

class DirectoryReferenceLayerRepositoryTest {
    @Test
    fun getAll_returnsAllSupportedLayersInTheDirectory() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)
        val file3 = TempFiles.createTempFile(dir)
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file1, true, file1.name)
            it.addFile(file2, false, file2.name)
            it.addFile(file3, true, file3.name)
        }

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAll_returnsAllSupportedLayersInSubDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir(dir1)
        val file1 = TempFiles.createTempFile(dir2)
        val file2 = TempFiles.createTempFile(dir2)
        val file3 = TempFiles.createTempFile(dir2)
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file1, true, file1.name)
            it.addFile(file2, false, file2.name)
            it.addFile(file3, true, file3.name)
        }

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, "") { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAll_withMultipleDirectories_returnsAllSupportedLayersInAllDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir1)
        val file3 = TempFiles.createTempFile(dir2)
        val file4 = TempFiles.createTempFile(dir2)
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file1, true, file1.name)
            it.addFile(file2, false, file2.name)
            it.addFile(file3, true, file3.name)
            it.addFile(file4, false, file4.name)
        }

        val repository =
            DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file3))
    }

    /**
     * We do this so we don't end up returns a list of reference layers with non-unique IDs. If two
     * (or more) files have the same relative path, only the first one (in the order of declared
     * layer directories) will be returned.
     */
    @Test
    fun getAll_withMultipleDirectoriesWithFilesWithTheSameRelativePath_onlyReturnsTheSupportedFileFromTheFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        val file2 = TempFiles.createTempFile(dir2, "blah", ".temp")
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file1, true, file1.name)
            it.addFile(file2, true, file2.name)
        }

        val repository =
            DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1))
    }

    @Test
    fun get_returnsLayer() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file1, true, file1.name)
            it.addFile(file2, true, file2.name)
        }

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectories_returnsLayer() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file1, true, file1.name)
            it.addFile(file2, true, file2.name)
        }

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectoriesWithFilesWithTheSameRelativePath_returnsLayerFromFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        val file2 = TempFiles.createTempFile(dir2, "blah", ".temp")
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file1, true, file1.name)
            it.addFile(file2, true, file2.name)
        }

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        val layerId = repository.getAll().first().id
        assertThat(repository.get(layerId)!!.file, equalTo(file1))
    }

    @Test
    fun get_whenFileDoesNotExist_returnsNull() {
        val dir = TempFiles.createTempDir()
        val file = TempFiles.createTempFile(dir)
        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file, true, file.name)
        }

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        val fileLayer = repository.getAll().first { it.file == file }

        file.delete()
        assertThat(repository.get(fileLayer.id), equalTo(null))
    }

    @Test
    fun get_returnsLayerWithCorrectName() {
        val dir = TempFiles.createTempDir()
        val file = TempFiles.createTempFile(dir)

        val mapConfigurator = StubMapConfigurator().also {
            it.addFile(file, true, file.name)
        }

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        val fileLayer = repository.getAll().first { it.file == file }

        assertThat(repository.get(fileLayer.id)!!.name, equalTo(file.name))
    }

    @Test
    fun addLayer_movesFileToTheSharedLayersDir_whenSharedIsTrue() {
        val sharedLayersDir = TempFiles.createTempDir()
        val projectLayersDir = TempFiles.createTempDir()
        val file = TempFiles.createTempFile().also {
            it.writeText("blah")
        }

        val repository = DirectoryReferenceLayerRepository(
            sharedLayersDir.absolutePath,
            projectLayersDir.absolutePath
        ) { StubMapConfigurator() }

        repository.addLayer(file, true)

        assertThat(sharedLayersDir.listFiles().size, equalTo(1))
        assertThat(sharedLayersDir.listFiles()[0].name, equalTo(file.name))
        assertThat(sharedLayersDir.listFiles()[0].readText(), equalTo("blah"))
        assertThat(projectLayersDir.listFiles().size, equalTo(0))
    }

    @Test
    fun addLayer_movesFileToTheProjectLayersDir_whenSharedIsFalse() {
        val sharedLayersDir = TempFiles.createTempDir()
        val projectLayersDir = TempFiles.createTempDir()
        val file = TempFiles.createTempFile().also {
            it.writeText("blah")
        }

        val repository = DirectoryReferenceLayerRepository(
            sharedLayersDir.absolutePath,
            projectLayersDir.absolutePath
        ) { StubMapConfigurator() }

        repository.addLayer(file, false)

        assertThat(sharedLayersDir.listFiles().size, equalTo(0))
        assertThat(projectLayersDir.listFiles().size, equalTo(1))
        assertThat(projectLayersDir.listFiles()[0].name, equalTo(file.name))
        assertThat(projectLayersDir.listFiles()[0].readText(), equalTo("blah"))
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
