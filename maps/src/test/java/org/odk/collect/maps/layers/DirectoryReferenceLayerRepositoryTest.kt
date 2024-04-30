package org.odk.collect.maps.layers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.shared.TempFiles

class DirectoryReferenceLayerRepositoryTest {

    @Test
    fun getAll_returnsAllLayersInTheDirectory() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(listOf(dir.absolutePath), mock())
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    @Test
    fun getAll_returnsAllLayersInSubDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir(dir1)
        val file1 = TempFiles.createTempFile(dir2)
        val file2 = TempFiles.createTempFile(dir2)

        val repository = DirectoryReferenceLayerRepository(listOf(dir1.absolutePath), mock())
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    @Test
    fun getAll_withMultipleDirectories_returnsAllLayersInAllDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)

        val repository = DirectoryReferenceLayerRepository(listOf(dir1.absolutePath, dir2.absolutePath), mock())
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    /**
     * We do this so we don't end up returns a list of reference layers with non-unique IDs. If two
     * (or more) files have the same relative path, only the first one (in the order of declared
     * layer directories) will be returned.
     */
    @Test
    fun getAll_withMultipleDirectoriesWithFilesWithTheSameRelativePath_onlyReturnsTheFileFromTheFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        TempFiles.createTempFile(dir2, "blah", ".temp")

        val repository = DirectoryReferenceLayerRepository(listOf(dir1.absolutePath, dir2.absolutePath), mock())
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1))
    }

    @Test
    fun getAllSupported_returnsAllSupportedLayersInTheDirectory() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)
        val file3 = TempFiles.createTempFile(dir)
        val mapConfigurator = mock<MapConfigurator>().also {
            whenever(it.supportsLayer(file1)).thenReturn(true)
            whenever(it.supportsLayer(file2)).thenReturn(false)
            whenever(it.supportsLayer(file3)).thenReturn(true)
        }

        val repository = DirectoryReferenceLayerRepository(listOf(dir.absolutePath)) { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAllSupported_returnsAllSupportedLayersInSubDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir(dir1)
        val dir3 = TempFiles.createTempDir(dir1)
        val file1 = TempFiles.createTempFile(dir2)
        val file2 = TempFiles.createTempFile(dir2)
        val file3 = TempFiles.createTempFile(dir3)
        val mapConfigurator = mock<MapConfigurator>().also {
            whenever(it.supportsLayer(file1)).thenReturn(true)
            whenever(it.supportsLayer(file2)).thenReturn(false)
            whenever(it.supportsLayer(file3)).thenReturn(true)
        }

        val repository = DirectoryReferenceLayerRepository(listOf(dir1.absolutePath)) { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAllSupported_withMultipleDirectories_returnsAllSupportedLayersInAllDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val dir3 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)
        val file3 = TempFiles.createTempFile(dir3)
        val mapConfigurator = mock<MapConfigurator>().also {
            whenever(it.supportsLayer(file1)).thenReturn(true)
            whenever(it.supportsLayer(file2)).thenReturn(false)
            whenever(it.supportsLayer(file3)).thenReturn(true)
        }

        val repository =
            DirectoryReferenceLayerRepository(listOf(dir1.absolutePath, dir2.absolutePath, dir3.absolutePath)) { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1, file3))
    }

    /**
     * We do this so we don't end up returns a list of reference layers with non-unique IDs. If two
     * (or more) files have the same relative path, only the first one (in the order of declared
     * layer directories) will be returned.
     */
    @Test
    fun getAllSupported_withMultipleDirectoriesWithFilesWithTheSameRelativePath_onlyReturnsTheSupportedFileFromTheFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val dir3 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        val file2 = TempFiles.createTempFile(dir2, "blah", ".temp")
        val file3 = TempFiles.createTempFile(dir3, "blah", ".temp")
        val mapConfigurator = mock<MapConfigurator>().also {
            whenever(it.supportsLayer(file1)).thenReturn(true)
            whenever(it.supportsLayer(file2)).thenReturn(false)
            whenever(it.supportsLayer(file3)).thenReturn(true)
        }

        val repository =
            DirectoryReferenceLayerRepository(listOf(dir1.absolutePath, dir2.absolutePath, dir3.absolutePath)) { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1))
    }

    @Test
    fun get_returnsLayer() {
        val dir = TempFiles.createTempDir()
        TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(listOf(dir.absolutePath), mock())
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectories_returnsLayer() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)

        val repository = DirectoryReferenceLayerRepository(listOf(dir1.absolutePath, dir2.absolutePath), mock())
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectoriesWithFilesWithTheSameRelativePath_returnsLayerFromFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        TempFiles.createTempFile(dir2, "blah", ".temp")

        val repository = DirectoryReferenceLayerRepository(listOf(dir1.absolutePath, dir2.absolutePath), mock())
        val layerId = repository.getAll().first().id
        assertThat(repository.get(layerId)!!.file, equalTo(file1))
    }

    @Test
    fun get_whenFileDoesNotExist_returnsNull() {
        val dir = TempFiles.createTempDir()
        val file = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(listOf(dir.absolutePath), mock())
        val fileLayer = repository.getAll().first { it.file == file }

        file.delete()
        assertThat(repository.get(fileLayer.id), equalTo(null))
    }
}
