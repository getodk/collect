package org.odk.collect.android.database.helpers;

import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.database.helpers.InstancesDatabaseHelper.DATABASE_PATH;
import static org.odk.collect.android.database.helpers.InstancesDatabaseHelper.INSTANCES_TABLE_NAME;
import static org.odk.collect.android.database.helpers.SqlLiteHelperTest.Action.DOWNGRADE;
import static org.odk.collect.android.database.helpers.SqlLiteHelperTest.Action.UPGRADE;
import static org.odk.collect.android.test.FileUtils.copyFileFromAssets;

@RunWith(Parameterized.class)
public class InstancesDatabaseHelperTest extends SqlLiteHelperTest {
    @Parameterized.Parameter(1)
    public String description;

    @Parameterized.Parameter(2)
    public String dbFilename;

    @Before
    public void saveRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH), new File(DATABASE_PATH + TEMPORARY_EXTENSION));
    }

    @After
    public void restoreRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH + TEMPORARY_EXTENSION), new File(DATABASE_PATH));
    }

    @Parameterized.Parameters(name = "{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {DOWNGRADE, "Downgrading from 5+ to version 5 should updated database paths", "instances_v6.db"},

                {UPGRADE, "Upgrading from version with extra column drops that column", "instances_v3.db"},
                {UPGRADE, "Upgrading from version with missing column adds that column", "instances_v4_removed_jrVersion.db"}
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

        if (action.equals(UPGRADE)) {
            assertThatInstancesAreKeptAfterMigrating();
        } else {
            assertThatInstanceFilePathsAreUpdated(db);
        }
    }

    private void assertThatInstanceFilePathsAreUpdated(SQLiteDatabase db) {
        List<Instance> instances = new InstancesDao().getInstancesFromCursor(db.query(INSTANCES_TABLE_NAME, null, null, null, null, null, null));
        assertEquals(instances.size(), 4);
        for (Instance instance : instances) {
            assertTrue(instance.getInstanceFilePath().startsWith(Collect.INSTANCES_PATH));
        }
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
