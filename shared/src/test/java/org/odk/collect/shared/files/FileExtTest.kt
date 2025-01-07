package org.odk.collect.shared.files

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.files.FileExt.listFilesRecursively
import org.odk.collect.shared.files.FileExt.sanitizedCanonicalPath
import org.odk.collect.shared.files.FileExt.saveToFile
import java.io.File

class FileExtTest {
    @Test
    fun `sanitizedCanonicalPath returns sanitized canonicalPath if androidData path segment is incorrect`() {
        val file = mock<File>().apply {
            whenever(canonicalPath).thenReturn("/storage/emulated/0/android/Data/org.odk.collect.android/files/projects/DEMO/blah")
        }

        assertThat(file.sanitizedCanonicalPath(), equalTo("/storage/emulated/0/Android/data/org.odk.collect.android/files/projects/DEMO/blah"))
    }

    @Test
    fun `sanitizedCanonicalPath returns original canonicalPath if androidData path segment is correct`() {
        val file = mock<File>().apply {
            whenever(canonicalPath).thenReturn("/storage/emulated/0/Android/data/org.odk.collect.android/files/projects/DEMO/blah")
        }

        assertThat(file.sanitizedCanonicalPath(), equalTo(file.canonicalPath))
    }

    @Test
    fun `sanitizedCanonicalPath returns original canonicalPath if other part of the path than the androidData is incorrect`() {
        val file = mock<File>().apply {
            whenever(canonicalPath).thenReturn("/Storage/Emulated/0/Android/data/org.odk.collect.android/files/projects/DEMO/blah")
        }

        assertThat(file.sanitizedCanonicalPath(), equalTo(file.canonicalPath))
    }

    @Test
    fun `sanitizedCanonicalPath returns original canonicalPath if it does not contain the androidData part`() {
        val file = mock<File>().apply {
            whenever(canonicalPath).thenReturn("/data/data/DEMO/files/blah")
        }

        assertThat(file.sanitizedCanonicalPath(), equalTo(file.canonicalPath))
    }

    @Test
    fun `saveToFile saves data to file`() {
        val file = TempFiles.createTempFile().apply {
            saveToFile("blah".byteInputStream())
        }

        assertThat(file.readText(), equalTo("blah"))
    }

    @Test
    fun listFilesRecursively_withEmptyDirectory_returnsEmptyList() {
        val files = TempFiles.createTempDir().listFilesRecursively()
        assertThat(files.size, equalTo(0))
    }

    @Test
    fun listFilesRecursively_withFilesInDirectory_returnsFiles() {
        val directory = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(directory, "blah1", ".txt")
        val file2 = TempFiles.createTempFile(directory, "blah2", ".txt")

        val files = directory.listFilesRecursively()
        assertThat(files, containsInAnyOrder(file1, file2))
    }

    @Test
    fun listFilesRecursively_withFilesAndEmptyDirectoryInDirectory_returnsFiles() {
        val directory = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(directory, "blah1", ".txt")
        val file2 = TempFiles.createTempFile(directory, "blah2", ".txt")
        File(directory, "blah").mkdir()

        val files = directory.listFilesRecursively()
        assertThat(files, containsInAnyOrder(file1, file2))
    }

    @Test
    fun listFilesRecursively_withFilesAndADirectoryWithFilesInDirectory_returnsFilesFromBothDirectories() {
        val directory1 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(directory1, "blah1", ".txt")
        val file2 = TempFiles.createTempFile(directory1, "blah2", ".txt")

        val directory2 = File(directory1, "blah").also { it.mkdir() }
        val file3 = TempFiles.createTempFile(directory2, "blah3", ".txt")

        val files = directory1.listFilesRecursively()
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

        val files = directory1.listFilesRecursively()
        assertThat(files, containsInAnyOrder(file1, file2, file3, file4))
    }

    @Test
    fun listFilesRecursively_whenDirectoryDoesNotExist_returnsEmptyList() {
        val directory = File(TempFiles.getPathInTempDir())
        val files = directory.listFilesRecursively()
        assertThat(files.size, equalTo(0))
    }
}
