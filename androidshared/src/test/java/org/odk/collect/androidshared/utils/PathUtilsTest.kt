package org.odk.collect.androidshared.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.PathUtils
import org.odk.collect.shared.TempFiles
import java.io.File

class PathUtilsTest {
    @Test
    fun `getAbsoluteFilePath() returns filePath prepended with dirPath`() {
        val path = PathUtils.getAbsoluteFilePath("/anotherRoot/anotherDir", "root/dir/file")
        assertThat(path, equalTo("/anotherRoot/anotherDir/root/dir/file"))
    }

    @Test
    fun `getAbsoluteFilePath() returns valid path when filePath does not start with seperator`() {
        val path = PathUtils.getAbsoluteFilePath("/root/dir", "file")
        assertThat(path, equalTo("/root/dir/file"))
    }

    @Test
    fun `getAbsoluteFilePath() returns filePath when it starts with dirPath`() {
        val path = PathUtils.getAbsoluteFilePath("/root/dir", "/root/dir/file")
        assertThat(path, equalTo("/root/dir/file"))
    }

    @Test
    fun `getAbsoluteFilePath() works when dirPath is not canonical`() {
        val tempDir = TempFiles.createTempDir()
        val nonCanonicalPath =
            tempDir.canonicalPath + File.separator + ".." + File.separator + tempDir.name
        assertThat(File(nonCanonicalPath).canonicalPath, equalTo(tempDir.canonicalPath))

        val path = PathUtils.getAbsoluteFilePath(nonCanonicalPath, "file")
        assertThat(path, equalTo(nonCanonicalPath + File.separator + "file"))
    }
}
