package org.odk.collect.android;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.utilities.CompressionUtils;

import java.io.IOException;
import java.util.zip.DataFormatException;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests to verify the behaviour of compression/decompression of text
 */

public class CompressionTest {

    private String text;
    private String compressedText;
    private String decompressedText;

    @Before
    public void setUp() {
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
                "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore " +
                "eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt " +
                "in culpa qui officia deserunt mollit anim id est laborum.";
    }

    @After
    public void tearDown() {
        compressedText = "";
        decompressedText = "";
    }


    @Test
    public void compressText() throws IOException {
        compressedText = CompressionUtils.compress(text);
        assertTrue(compressedText.length() < text.length());
    }

    @Test
    public void compressNullText() throws IOException {
        String null_text = "";
        compressedText = CompressionUtils.compress(null_text);
        assertTrue(null_text.equals(compressedText));
    }

    @Test
    public void decompress() throws IOException, DataFormatException {
        compressedText = CompressionUtils.compress(text);
        decompressedText = CompressionUtils.decompress(compressedText);
        assertTrue(text.equals(decompressedText));
    }


    @Test
    public void decompressNullText() throws IOException, DataFormatException {
        String null_text = "";
        decompressedText = CompressionUtils.decompress(null_text);
        assertTrue(null_text.equals(decompressedText));
    }

    @Test(expected=DataFormatException.class)
    public void decompressRaiseException() throws IOException, DataFormatException {
        String input = "Decoding this will raise an error";
        decompressedText = CompressionUtils.decompress(input);
    }
}
