package org.odk.collect.shared.files

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.shared.files.FileExt.sanitizedCanonicalPath
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
}
