package org.odk.collect.android.test;

import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class TestUtils {
    private TestUtils() {}

    public static File createTempFile(String content) throws Exception {
        File f = createTempFile();

        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(content);
        } finally {
            closeSafely(fw);
        }

        return f;
    }

    public static File createTempFile() throws Exception {
        // Create our own directory, because 2-arg `createTempFile()` sometimes fails with:
        //     java.io.IOException: open failed: ENOENT (No such file or directory)
        File dir = new File(Environment.getExternalStorageDirectory(), "test");
        dir.mkdirs();
        File tempFile = File.createTempFile("tst", null, dir);
        dir.deleteOnExit(); // Not fail-safe on android )¬;
        return tempFile;
    }

    public static void closeSafely(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                // not much you can do at this point
            }
        }
    }

    public static void assertMatches(String expectedPattern, Object actual) {
        if (!testMatches(expectedPattern, actual)) {
            throw new AssertionError(String.format("Expected <%s> to match <%s>.", actual, expectedPattern));
        }
    }

    public static void assertMatches(String message, String expectedPattern, Object actual) {
        if (!testMatches(expectedPattern, actual)) {
            throw new AssertionError(String.format("%s  Expected <%s> to match <%s>.", message, actual, expectedPattern));
        }
    }

    private static boolean testMatches(String expectedPattern, Object actual) {
        if (expectedPattern == null) {
            throw new IllegalArgumentException("No pattern provided.");
        }
        if (actual == null) {
            return false;
        }
        return actual.toString().matches(expectedPattern);
    }
}
