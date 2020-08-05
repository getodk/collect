package org.odk.collect.android.instrumented.database.helpers;

import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.InstancesDatabaseHelper;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.database.InstancesDatabaseHelper.INSTANCES_TABLE_NAME;
import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

@RunWith(Parameterized.class)
@Ignore("`Parameterized` causes problems for Firebase sharding. Probably need to replace this at JUnit level")
public class InstancesDatabaseHelperTest extends SqlLiteHelperTest {
    private static final String DATABASE_PATH = InstancesDatabaseHelper.getDatabasePath();

    @Parameterized.Parameter
    public String description;

    /**
     * SQLite file that should contain exactly two instances:
     * - one complete instance with date field set to 1564413556249
     * - one incomplete instance with date field set to 1564413579406
     */
    @Parameterized.Parameter(1)
    public String dbFilename;

    @Before
    public void saveRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH), new File(DATABASE_PATH + TEMPORARY_EXTENSION));
    }

    @After
    public void restoreRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH + TEMPORARY_EXTENSION), new File(DATABASE_PATH));
        FileUtils.deleteAndReport(new File(DATABASE_PATH + TEMPORARY_EXTENSION));
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Downgrading from version with extra column drops that column", "instances_v7000_added_fakeColumn.db"},
                {"Downgrading from version with missing column adds that column", "instances_v7000_removed_jrVersion.db"},

                {"Upgrading from version with extra column drops that column", "instances_v3.db"},
                {"Upgrading from version with missing column adds that column", "instances_v4_removed_jrVersion.db"},

                {"Upgrading from v5 results in current version columns", "instances_v5.db"}
        });
    }

    @Test
    public void testMigration() throws IOException {
        copyFileFromAssets("database" + File.separator + dbFilename, DATABASE_PATH);
        InstancesDatabaseHelper databaseHelper = new InstancesDatabaseHelper();
        ensureMigrationAppliesFully(databaseHelper);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        assertThat(db.getVersion(), is(InstancesDatabaseHelper.DATABASE_VERSION));

        List<String> newColumnNames = SQLiteUtils.getColumnNames(db, INSTANCES_TABLE_NAME);
        assertThat(newColumnNames, contains(InstancesDatabaseHelper.CURRENT_VERSION_COLUMN_NAMES));
        assertThatInstancesAreKeptAfterMigrating();
    }

    private void assertThatInstancesAreKeptAfterMigrating() {
        InstancesDao instancesDao = new InstancesDao();
        List<Instance> instances = instancesDao.getInstancesFromCursor(instancesDao.getInstancesCursor(null, null));
        assertEquals(2, instances.size());
        assertEquals("complete", instances.get(0).getStatus());
        assertEquals(Long.valueOf(1564413556249L), instances.get(0).getLastStatusChangeDate());
        assertEquals("incomplete", instances.get(1).getStatus());
        assertEquals(Long.valueOf(1564413579406L), instances.get(1).getLastStatusChangeDate());
    }
}
