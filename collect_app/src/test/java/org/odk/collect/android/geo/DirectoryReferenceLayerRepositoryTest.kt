package org.odk.collect.android.geo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.TempFiles

class DirectoryReferenceLayerRepositoryTest {

    @Test
    fun getAll_returnsAllLayersInTheDirectory() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath)
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    @Test
    fun getAll_returnsAllLayersInSubDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir(dir1)
        val file1 = TempFiles.createTempFile(dir2)
        val file2 = TempFiles.createTempFile(dir2)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath)
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    @Test
    fun getAll_withMultipleDirectories_returnsAllLayersInAllDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath)
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

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath)
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1))
    }

    @Test
    fun get_returnsLayer() {
        val dir = TempFiles.createTempDir()
        TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath)
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectories_returnsLayer() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath)
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectoriesWithFilesWithTheSameRelativePath_returnsLayerFromFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        TempFiles.createTempFile(dir2, "blah", ".temp")

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath)
        val layerId = repository.getAll().first().id
        assertThat(repository.get(layerId)!!.file, equalTo(file1))
    }

    @Test
    fun get_whenFileDoesNotExist_returnsNull() {
        val dir = TempFiles.createTempDir()
        val file = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath)
        val fileLayer = repository.getAll().first { it.file == file }

        file.delete()
        assertThat(repository.get(fileLayer.id), equalTo(null))
    }
}
