package org.odk.collect.shared

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

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
    fun `getRelativeFilePath() returns filePath with dirPath removed`() {
        val path = PathUtils.getRelativeFilePath("/root/dir", "/root/dir/file")
        assertThat(path, equalTo("file"))
    }

    @Test
    fun `getRelativeFilePath() returns filePath when it does not start with dirPath`() {
        val path = PathUtils.getRelativeFilePath("/anotherRoot/anotherDir", "/root/dir/file")
        assertThat(path, equalTo("/root/dir/file"))
    }

    @Test
    fun `reserved chars should be removed from file name`() {
        val result = PathUtils.getPathSafeFileName("P\"1*ą/ć:!<@>#?$\\%|^&[]{}_=+")
        assertThat(result, equalTo("P_1_ą_ć_!_@_#_\$_%_^&[]{}_=+"))
    }
}
