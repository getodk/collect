package org.odk.collect.android.utilities;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FileUtilsTest {
    @Test
    public void md5HashIsCorrect() throws NoSuchAlgorithmException, IOException {
        String contents = "Hello, world";
        File tempFile = File.createTempFile("hello", "txt");
        tempFile.deleteOnExit();
        FileWriter fw = new FileWriter(tempFile);
        fw.write(contents);
        fw.close();
        for (int bufSize : Arrays.asList(1, contents.length() - 1, contents.length(), 64 * 1024)) {
            FileUtils.bufSize = bufSize;
            String expectedResult = "bc6e6f16b8a077ef5fbc8d59d0b931b9";  // From md5 command-line utility
            assertEquals(expectedResult, FileUtils.getMd5Hash(tempFile));
        }
    }

    @Test
    public void mediaDirNameIsCorrect() {
        String expected = "sample-file-media";

        assertEquals(expected, FileUtils.constructMediaPath("sample-file.xml"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.extension"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.123"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.docx"));
    }
}
