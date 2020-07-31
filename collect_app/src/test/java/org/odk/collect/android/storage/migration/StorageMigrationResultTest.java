package org.odk.collect.android.storage.migration;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
public class StorageMigrationResultTest {

    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void getErrorResultMessageTest() {
        assertThat(StorageMigrationResult.NOT_ENOUGH_SPACE.getErrorResultMessage(context),
                is("Error: You do not have enough space on your internal storage to migrate your data. Please free up some space and try again."));

        assertThat(StorageMigrationResult.MOVING_FILES_FAILED.getErrorResultMessage(context),
                is("Error: Data migration failed. Please try again."));

        assertThat(StorageMigrationResult.CHANGES_IN_PROGRESS.getErrorResultMessage(context),
                is("Error: Automatic send or blank form download is running. Please try again later."));
    }
}
