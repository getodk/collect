package org.odk.collect.android.test;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;

public final class TestUtils {
    private TestUtils() {}

    public static Map<String, ?> backupPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance().getBaseContext());
        return Collections.unmodifiableMap(prefs.getAll());
    }

    public static void restorePreferences(Map<String, ?> backup) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance().getBaseContext()).edit();

        editor.clear();

        for (Map.Entry<String, ?> e : backup.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Boolean) {
                editor.putBoolean(e.getKey(), (Boolean) v);
            } else if (v instanceof Float) {
                editor.putFloat(e.getKey(), (Float) v);
            } else if (v instanceof Integer) {
                editor.putInt(e.getKey(), (Integer) v);
            } else if (v instanceof Long) {
                editor.putLong(e.getKey(), (Long) v);
            } else if (v instanceof String) {
                editor.putString(e.getKey(), (String) v);
            } else if (v instanceof Set) {
                editor.putStringSet(e.getKey(), (Set<String>) v);
            } else {
                throw new RuntimeException("Unhandled preference value type: " + v);
            }
        }

        editor.apply();
    }

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
