package org.odk.collect.android.test;

import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;

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
        File dir = tempFileDirectory();
        dir.mkdirs();
        File tempFile = File.createTempFile("tst", null, dir);
        dir.deleteOnExit(); // Not fail-safe on android )¬;
        return tempFile;
    }

    public static void cleanUpTempFiles() {
        File[] tempFiles = tempFileDirectory().listFiles();
        if (tempFiles != null) {
            for (File f : tempFiles) {
                f.delete();
            }
        }
    }

    private static File tempFileDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "test-tmp");
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

    public static void resetInstancesContentProvider() {
        Collect.getInstance().getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null);
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
