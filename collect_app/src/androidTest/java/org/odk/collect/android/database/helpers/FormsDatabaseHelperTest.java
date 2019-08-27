package org.odk.collect.android.database.helpers;

import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.database.helpers.FormsDatabaseHelper.DATABASE_PATH;
import static org.odk.collect.android.test.FileUtils.copyFileFromAssets;

@RunWith(Parameterized.class)
public class FormsDatabaseHelperTest extends SqlLiteHelperTest {
    @Parameterized.Parameter
    public String description;

    @Parameterized.Parameter(1)
    public String dbFilename;

    @Before
    public void saveRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH), new File(DATABASE_PATH + TEMPORARY_EXTENSION));
    }

    @After
    public void restoreRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH + TEMPORARY_EXTENSION), new File(DATABASE_PATH));
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Downgrading from version with extra column drops that column", "forms_v7000_added_fakeColumn.db"},
                {"Downgrading from version with missing column adds that column", "forms_v7000_removed_jrVersion.db"},

                {"Upgrading from version with extra column and missing columns", "forms_v4.db"},
                {"Upgrading from version with the same columns", "forms_v4_with_columns_from_v7.db"}
        });
    }

    @Test
    public void testMigration() throws IOException {
        copyFileFromAssets("database" + File.separator + dbFilename, DATABASE_PATH);
        FormsDatabaseHelper databaseHelper = new FormsDatabaseHelper();
        ensureMigrationAppliesFully(databaseHelper);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        assertThat(db.getVersion(), is(FormsDatabaseHelper.DATABASE_VERSION));

        List<String> newColumnNames = FormsDatabaseHelper.getFormsColumnNames(db);

        assertThat(newColumnNames, contains(FormsDatabaseHelper.CURRENT_VERSION_COLUMN_NAMES));
    }
}