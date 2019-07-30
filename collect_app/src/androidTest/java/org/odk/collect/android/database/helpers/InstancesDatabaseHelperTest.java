package org.odk.collect.android.database.helpers;

import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class InstancesDatabaseHelperTest {
    @Parameterized.Parameter
    public String description;

    @Parameterized.Parameter(1)
    public String dbFilename;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Downgrading from version with extra column drops that column", "instances_v7000_added_fakeColumn.db"},
                {"Downgrading from version with missing column adds that column", "instances_v7000_removed_jrVersion.db"},

                {"Upgrading from version with extra column drops that column", "instances_v4_real.db"},
                {"Upgrading from version with missing column adds that column", "instances_v4_removed_jrVersion.db"}
        });
    }

    private static final String DATABASE_PATH = Collect.METADATA_PATH + File.separator + InstancesDatabaseHelper.DATABASE_NAME;
    private static final String TEMPORARY_EXTENSION = ".real";

    @Before
    public void saveRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH), new File(DATABASE_PATH + TEMPORARY_EXTENSION));
    }

    @After
    public void restoreRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH + TEMPORARY_EXTENSION), new File(DATABASE_PATH));
    }

    @Test
    public void testMigration() throws IOException {
        writeDatabaseFile("database" + File.separator + dbFilename);
        InstancesDatabaseHelper databaseHelper = new InstancesDatabaseHelper();
        ensureMigrationAppliesFully(databaseHelper);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        assertThat(db.getVersion(), is(InstancesDatabaseHelper.DATABASE_VERSION));

        List<String> newColumnNames = InstancesDatabaseHelper.getInstancesColumnNames(db);

        assertThat(newColumnNames, contains(InstancesDatabaseHelper.CURRENT_VERSION_COLUMN_NAMES));
    }

    private void writeDatabaseFile(String dbPath) throws IOException {
        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        try (InputStream input = assetManager.open(dbPath);
             OutputStream output = new FileOutputStream(DATABASE_PATH)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        }
    }

    /**
     * Gets a read-only reference to the instances database and then immediately releases it.
     *
     * Without this, it appears that the migrations only get partially applied. It's not clear how
     * this is possible since calls to onDowngrade and onUpgrade are wrapped in transactions. See
     * discussion at https://github.com/opendatakit/collect/pull/3250#issuecomment-516439704
     */
    private void ensureMigrationAppliesFully(InstancesDatabaseHelper databaseHelper) {
        databaseHelper.getReadableDatabase().close();
    }
}
