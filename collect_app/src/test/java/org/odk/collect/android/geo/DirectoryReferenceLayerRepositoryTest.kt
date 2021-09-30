package org.odk.collect.android.geo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import org.odk.collect.shared.TempFiles

class DirectoryReferenceLayerRepositoryTest {

    @Test
    fun getAll_returnsAllFilesInTheDirectory() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath)
        assertThat(repository.getAll(), Matchers.containsInAnyOrder(file1, file2))
    }

    @Test
    fun getAll_returnsAllFilesInSubDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir(dir1)
        val file1 = TempFiles.createTempFile(dir2)
        val file2 = TempFiles.createTempFile(dir2)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath)
        assertThat(repository.getAll(), Matchers.containsInAnyOrder(file1, file2))
    }
}
