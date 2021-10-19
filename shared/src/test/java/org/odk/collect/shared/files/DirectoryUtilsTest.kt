package org.odk.collect.shared.files

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.TempFiles
import java.io.File

class DirectoryUtilsTest {

    @Test
    fun listFilesRecursively_withEmptyDirectory_returnsEmptyList() {
        val files = DirectoryUtils.listFilesRecursively(TempFiles.createTempDir())
        assertThat(files.size, equalTo(0))
    }

    @Test
    fun listFilesRecursively_withFilesInDirectory_returnsFiles() {
        val directory = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(directory, "blah1", ".txt")
        val file2 = TempFiles.createTempFile(directory, "blah2", ".txt")

        val files = DirectoryUtils.listFilesRecursively(directory)
        assertThat(files, containsInAnyOrder(file1, file2))
    }

    @Test
    fun listFilesRecursively_withFilesAndEmptyDirectoryInDirectory_returnsFiles() {
        val directory = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(directory, "blah1", ".txt")
        val file2 = TempFiles.createTempFile(directory, "blah2", ".txt")
        File(directory, "blah").mkdir()

        val files = DirectoryUtils.listFilesRecursively(directory)
        assertThat(files, containsInAnyOrder(file1, file2))
    }

    @Test
    fun listFilesRecursively_withFilesAndADirectoryWithFilesInDirectory_returnsFilesFromBothDirectories() {
        val directory1 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(directory1, "blah1", ".txt")
        val file2 = TempFiles.createTempFile(directory1, "blah2", ".txt")

        val directory2 = File(directory1, "blah").also { it.mkdir() }
        val file3 = TempFiles.createTempFile(directory2, "blah3", ".txt")

        val files = DirectoryUtils.listFilesRecursively(directory1)
        assertThat(files, containsInAnyOrder(file1, file2, file3))
    }

    @Test
    fun listFilesRecursively_withFilesAndADirectoryContainingAnotherDirectoryWithFilesInDirectory_returnsFilesFromAllDirectories() {
        val directory1 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(directory1, "blah1", ".txt")
        val file2 = TempFiles.createTempFile(directory1, "blah2", ".txt")

        val directory2 = File(directory1, "blah").also { it.mkdir() }
        val file3 = TempFiles.createTempFile(directory2, "blah3", ".txt")

        val directory3 = File(directory2, "blah").also { it.mkdir() }
        val file4 = TempFiles.createTempFile(directory3, "blah4", ".txt")

        val files = DirectoryUtils.listFilesRecursively(directory1)
        assertThat(files, containsInAnyOrder(file1, file2, file3, file4))
    }

    @Test
    fun listFilesRecursively_whenDirectoryDoesNotExist_returnsEmptyList() {
        val directory = File(TempFiles.getPathInTempDir())
        val files = DirectoryUtils.listFilesRecursively(directory)
        assertThat(files.size, equalTo(0))
    }
}
