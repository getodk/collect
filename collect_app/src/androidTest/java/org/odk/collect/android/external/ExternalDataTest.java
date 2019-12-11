package org.odk.collect.android.external;

import android.Manifest;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExternalDataTest {
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME = "simple-search-external-csv.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_NAME = "simple-search-external-csv-fruits";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME = "simple-search-external-csv-fruits.csv";
    private static final String SIMPLE_SEARCH_EXTERNAL_DB_FILENAME = "simple-search-external-csv-fruits.db";

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME, Collections.singletonList(SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME)));

    @Test
    public void testCreateDBonImportCSV() {
        final File formPath = new File(Collect.FORMS_PATH + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME);
        final File mediaDir = FileUtils.getFormMediaDir(formPath);
        final File csvFile = new File(mediaDir + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME);

        Map<String, File> externalDataMap = new HashMap<>();
        externalDataMap.put(SIMPLE_SEARCH_EXTERNAL_CSV_NAME, csvFile);

        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);

        // Check expected table and metadata table
        final File dbFile = new File(mediaDir + File.separator + SIMPLE_SEARCH_EXTERNAL_DB_FILENAME);
        Assert.assertTrue(dbFile.exists());
        SQLiteDatabase.OpenParams.Builder paramsBuilder = new SQLiteDatabase.OpenParams.Builder();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile, paramsBuilder.build());
        Assert.assertNotNull(db);
        Assert.assertTrue(SQLiteUtils.doesTableExist(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME));
        Assert.assertTrue(SQLiteUtils.doesTableExist(db, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME));
    }

    @Test
    public void testCreateMetadata() throws Exception {
        final File formPath = new File(Collect.FORMS_PATH + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME);
        final File mediaDir = FileUtils.getFormMediaDir(formPath);
        final File csvFile = new File(mediaDir + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME);
        final String testMetadataTable = "testMetadataTable";

        SQLiteDatabase.OpenParams.Builder paramsBuilder = new SQLiteDatabase.OpenParams.Builder();
        SQLiteDatabase db = SQLiteDatabase.createInMemory(paramsBuilder.build());
        ExternalSQLiteOpenHelper.createAndPopulateMetadataTable(db, testMetadataTable, csvFile);
        Assert.assertTrue(SQLiteUtils.doesTableExist(db, testMetadataTable));

        // Check expected metadata table contents
        final String[] columnNames = {ExternalDataUtil.COLUMN_LAST_MODIFIED};
        final String selectCriteria = CustomSQLiteQueryBuilder.formatCompareEquals(
                ExternalDataUtil.COLUMN_DATASET_FILENAME,
                CustomSQLiteQueryBuilder.quoteStringLiteral(SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME));
        Cursor cursor = db.query(testMetadataTable, columnNames, selectCriteria, null, null, null, null);
        cursor.moveToFirst();
        long fileTimestamp = cursor.getLong(0);

        Assert.assertEquals(fileTimestamp, csvFile.lastModified());
    }
}
