package org.odk.collect.android.geo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.isNull
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
    fun get_returnsLayer() {
        val dir = TempFiles.createTempDir()
        TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath)
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
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
