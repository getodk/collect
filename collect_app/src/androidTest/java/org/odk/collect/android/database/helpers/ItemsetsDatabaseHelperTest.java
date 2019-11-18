package org.odk.collect.android.database.helpers;

import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.ItemsetDao;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.dto.Itemset;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.odk.collect.android.database.ItemsetDbAdapter.DATABASE_PATH;
import static org.odk.collect.android.database.ItemsetDbAdapter.ITEMSET_TABLE;
import static org.odk.collect.android.database.helpers.SqlLiteHelperTest.Action.DOWNGRADE;
import static org.odk.collect.android.test.FileUtils.copyFileFromAssets;

@RunWith(Parameterized.class)
public class ItemsetsDatabaseHelperTest extends SqlLiteHelperTest {
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
                {DOWNGRADE, "Downgrading from 2+ to version 2 should updated database paths", "itemsets_v3.db"}
        });
    }

    @Test
    public void testMigration() throws IOException {
        copyFileFromAssets("database" + File.separator + dbFilename, DATABASE_PATH);
        ItemsetDbAdapter.DatabaseHelper databaseHelper = new ItemsetDbAdapter.DatabaseHelper();
        ensureMigrationAppliesFully(databaseHelper);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        assertThat(db.getVersion(), is(ItemsetDbAdapter.DATABASE_VERSION));

        List<Itemset> itemsets = new ItemsetDao().getItemsetsFromCursor(db.query(ITEMSET_TABLE, null, null, null, null, null, null));
        assertEquals(itemsets.size(), 2);

        for (Itemset itemset : itemsets) {
            assertTrue(itemset.getPath().startsWith(Collect.ODK_ROOT));
        }
    }
}
