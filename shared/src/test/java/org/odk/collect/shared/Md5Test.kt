package org.odk.collect.shared

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.File
import java.io.FileWriter

class Md5Test {

    @Test
    fun md5HashIsCorrect() {
        val contents = "Hello, world"
        val tempFile = File.createTempFile("hello", "txt")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use {
            it.write(contents)
        }

        for (bufSize in listOf(1, contents.length - 1, contents.length, 64 * 1024)) {
            val expectedResult = "bc6e6f16b8a077ef5fbc8d59d0b931b9" // From md5 command-line utility
            assertThat(tempFile.getMd5Hash(bufSize), equalTo(expectedResult))
        }
    }
}
