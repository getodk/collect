package org.odk.collect.android.external;

import android.Manifest;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
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

    private File csvFile;
    private File dbFile;

    @Before
    public void setUp() {
        File formPath = new File(new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME);
        File mediaDir = FileUtils.getFormMediaDir(formPath);
        csvFile = new File(mediaDir + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME);
        dbFile = new File(mediaDir + File.separator + SIMPLE_SEARCH_EXTERNAL_DB_FILENAME);
    }

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME, Collections.singletonList(SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME)));

    private static Map<String, File> makeExternalDataMap(File csvFile) {
        Map<String, File> externalDataMap = new HashMap<>();
        externalDataMap.put(SIMPLE_SEARCH_EXTERNAL_CSV_NAME, csvFile);
        return externalDataMap;
    }

    private static SQLiteDatabase openDatabase(File databaseFile) {
        SQLiteDatabase.OpenParams.Builder paramsBuilder = new SQLiteDatabase.OpenParams.Builder();
        return SQLiteDatabase.openDatabase(databaseFile, paramsBuilder.build());
    }

    @Test
    public void testCreateDBonImportCSV() {
        Map<String, File> externalDataMap = makeExternalDataMap(csvFile);

        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);

        // Check expected table and metadata table
        Assert.assertTrue(dbFile.exists());
        SQLiteDatabase db = openDatabase(dbFile);
        Assert.assertNotNull(db);
        Assert.assertTrue(SQLiteUtils.doesTableExist(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME));
        Assert.assertTrue(SQLiteUtils.doesTableExist(db, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME));
    }

    @Test
    public void testCreateMetadata() throws Exception {
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

    @Test
    public void testReimportWhenDBisMissing() {
        Map<String, File> externalDataMap = new HashMap<>();
        externalDataMap.put(SIMPLE_SEARCH_EXTERNAL_CSV_NAME, csvFile);

        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        Assert.assertTrue(dbFile.exists());

        dbFile.delete();

        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        Assert.assertTrue(dbFile.exists());
    }

    @Test
    public void testImportWhenDBMetadataIsMissing() {
        Map<String, File> externalDataMap = makeExternalDataMap(csvFile);

        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        Assert.assertTrue(dbFile.exists());

        // Remove the metadata table (mimicking prior versions without the metadata table)
        SQLiteDatabase db = openDatabase(dbFile);
        SQLiteUtils.dropTable(db, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME);
        db.close();
        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        Assert.assertTrue(dbFile.exists());
        db = openDatabase(dbFile);
        Assert.assertTrue("metadata table should be recreated", SQLiteUtils.doesTableExist(db, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME));
        db.close();
    }

    @Test
    public void testImportWhenCSVisUpdated() {
        final String updateCriteria = CustomSQLiteQueryBuilder.formatCompareEquals(
                CustomSQLiteQueryBuilder.quoteIdentifier(ExternalDataUtil.COLUMN_DATASET_FILENAME),
                CustomSQLiteQueryBuilder.quoteStringLiteral(SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME));
        Map<String, File> externalDataMap = makeExternalDataMap(csvFile);

        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        Assert.assertTrue(dbFile.exists());

        // Fake an update by rewinding the clock on the database metadata timestamp field
        long originalTimestamp = csvFile.lastModified();
        long olderTimestamp = originalTimestamp - 1000;
        SQLiteDatabase db = openDatabase(dbFile);

        ContentValues metadata = new ContentValues();
        metadata.put(ExternalDataUtil.COLUMN_LAST_MODIFIED, olderTimestamp);
        db.update(ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME, metadata, updateCriteria, null);
        long metadataLastModified = ExternalSQLiteOpenHelper.getLastImportTimestamp(db, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME, csvFile);
        Assert.assertNotEquals("expected metadata table to reflect older timestamp", originalTimestamp, metadataLastModified);

        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        db = openDatabase(dbFile);

        // Check the metadata table import timestamp
        metadataLastModified = ExternalSQLiteOpenHelper.getLastImportTimestamp(db, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME, csvFile);
        Assert.assertEquals("expected metadata table to reflect last modified time", originalTimestamp, metadataLastModified);
    }

    @Test
    public void testSkipImportWhenCSVisUnchanged() {
        final String selectStarQuery =  "SELECT * FROM " + ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME;
        Map<String, File> externalDataMap = makeExternalDataMap(csvFile);

        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        Assert.assertTrue(dbFile.exists());
        SQLiteDatabase db = openDatabase(dbFile);
        Cursor cursor = db.rawQuery(selectStarQuery, null);
        Assert.assertNotNull(cursor);
        Assert.assertTrue(cursor.getCount() > 0);

        // Purge the contents of the data table before reimporting
        db.delete(ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, null, null);
        Assert.assertTrue(SQLiteUtils.doesTableExist(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME));
        cursor = db.rawQuery(selectStarQuery, null);
        Assert.assertNotNull(cursor);
        Assert.assertEquals("expected zero rows of data after purging", 0, cursor.getCount());
        db.close();

        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(externalDataMap);
        db = openDatabase(dbFile);
        cursor = db.rawQuery(selectStarQuery, null);
        Assert.assertNotNull(cursor);
        Assert.assertEquals("expected zero rows of data after reimporting unchanged file", 0, cursor.getCount());
    }
}
