package org.odk.collect.android.support;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.TreeIterables;

import org.hamcrest.Matcher;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.shared.Settings;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public final class TestUtils {
    private static final Settings GENERAL_PREFS = TestSettingsProvider.getGeneralSettings();

    private TestUtils() {

    }

    public static Map<String, ?> backupPreferences() {
        return Collections.unmodifiableMap(GENERAL_PREFS.getAll());
    }

    public static void restorePreferences(Map<String, ?> backup) {
        GENERAL_PREFS.clear();

        for (Map.Entry<String, ?> e : backup.entrySet()) {
            Object v = e.getValue();
            GENERAL_PREFS.save(e.getKey(), v);
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
        return new File(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES), "test-tmp");
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

    public static void resetInstances() {
        new InstancesRepositoryProvider(ApplicationProvider.getApplicationContext()).get().deleteAll();
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
