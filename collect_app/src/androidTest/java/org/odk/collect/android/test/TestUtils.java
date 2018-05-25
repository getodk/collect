package org.odk.collect.android.test;

import android.os.Environment;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.view.View;

import org.hamcrest.Matcher;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public final class TestUtils {
    private TestUtils() {
    }

    public static Map<String, ?> backupPreferences() {
        return Collections.unmodifiableMap(GeneralSharedPreferences.getInstance().getAll());
    }

    public static void restorePreferences(Map<String, ?> backup) {
        GeneralSharedPreferences.getInstance().clear();

        for (Map.Entry<String, ?> e : backup.entrySet()) {
            Object v = e.getValue();
            GeneralSharedPreferences.getInstance().save(e.getKey(), v);
        }
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

    /**
     * Performs action of waiting for a specific view id.
     * <p>
     * https://stackoverflow.com/a/22563297/638695
     */
    public static ViewAction waitId(final int viewId, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for a specific view with id <" + viewId + "> during " + millis + " millis.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                uiController.loopMainThreadUntilIdle();
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                final Matcher<View> viewMatcher = withId(viewId);

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return;
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);

                // timeout happens
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new TimeoutException())
                        .build();
            }
        };
    }
}
