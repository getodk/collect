package org.odk.collect.androidshared.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.zip.DataFormatException

@RunWith(AndroidJUnit4::class)
class CompressionUtilsTest {
    private val text = (
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
            "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
            "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore " +
            "eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt " +
            "in culpa qui officia deserunt mollit anim id est laborum."
        )

    @Test
    fun `compressed text should be shorter than the original one`() {
        val compressedText = CompressionUtils.compress(text)

        assertTrue(compressedText.length < text.length)
    }

    @Test
    fun `compressing an empty string returns an empty string`() {
        val nullText = ""
        val compressedText = CompressionUtils.compress(nullText)

        assertThat(nullText, equalTo(compressedText))
    }

    @Test
    fun `text after compressing and decompressing should be unchanged`() {
        val compressedText = CompressionUtils.compress(text)
        val decompressedText = CompressionUtils.decompress(compressedText)

        assertThat(text, equalTo(decompressedText))
    }

    @Test
    fun `decompressing an empty string returns an empty string`() {
        val emptyText = ""
        val decompressedText = CompressionUtils.decompress(emptyText)

        assertThat(emptyText, equalTo(decompressedText))
    }

    @Test(expected = DataFormatException::class)
    fun `decompressing a not compressed text throws an exception`() {
        val input = "Decoding this will raise an error"
        CompressionUtils.decompress(input)
    }
}
