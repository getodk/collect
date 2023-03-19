package org.odk.collect.android.externaldata;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.SQLiteUtils;
import org.odk.collect.shared.strings.Md5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.odk.collect.android.externaldata.ExternalDataUtil.COLUMN_DATASET_FILENAME;
import static org.odk.collect.android.externaldata.ExternalDataUtil.COLUMN_MD5_HASH;
import static org.odk.collect.android.externaldata.ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME;
import static org.odk.collect.android.externaldata.ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME;

@RunWith(AndroidJUnit4.class)
public class ExternalDataReaderTest {
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME = "simple-search-external-csv.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_NAME = "simple-search-external-csv-fruits";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME = "simple-search-external-csv-fruits.csv";
    private static final String SIMPLE_SEARCH_EXTERNAL_DB_FILENAME = "simple-search-external-csv-fruits.db";

    private static final String SELECT_ALL_DATA_QUERY = "SELECT * FROM " + EXTERNAL_DATA_TABLE_NAME;

    private static File csvFile;
    private static File dbFile;

    private static Map<String, File> formDefToCsvMedia;

    @Before
    public void setUp() throws IOException {
        CollectHelpers.setupDemoProject();

        File formFile = new File(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME);
        File mediaDir = FileUtils.getFormMediaDir(formFile);
        mediaDir.mkdir();
        csvFile = new File(mediaDir + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME);
        dbFile = new File(mediaDir + File.separator + SIMPLE_SEARCH_EXTERNAL_DB_FILENAME);
        formDefToCsvMedia = makeExternalDataMap();

        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("forms" + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM_FILENAME);
             OutputStream output = new FileOutputStream(formFile)) {
            IOUtils.copy(input, output);
        }

        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("media" + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME);
             OutputStream output = new FileOutputStream(csvFile)) {
            IOUtils.copy(input, output);
        }
    }

    @Test
    public void doImport_createsDataAndMetadataTables() {
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);

        assertThat(dbFile.exists(), is(true));

        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        assertThat(db, is(notNullValue()));
        assertThat(SQLiteUtils.doesTableExist(db, EXTERNAL_DATA_TABLE_NAME), is(true));
        assertThat(SQLiteUtils.doesTableExist(db, EXTERNAL_METADATA_TABLE_NAME), is(true));
    }

    /**
     * There are multiple features that ingest CSV files so the original file should not be modified.
     * https://github.com/getodk/collect/issues/3335
     */
    @Test
    public void doImport_doesNotModifyOriginalCsv() {
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);

        assertThat(dbFile.exists(), is(true));
        assertThat(csvFile.exists(), is(true));
    }

    @Test
    public void createAndPopulateMetadataTable_createsMetadataTableWithExpectedMd5Hash() {
        final String testMetadataTable = "testMetadataTable";

        SQLiteDatabase.OpenParams.Builder paramsBuilder = new SQLiteDatabase.OpenParams.Builder();
        SQLiteDatabase db = SQLiteDatabase.createInMemory(paramsBuilder.build());
        ExternalSQLiteOpenHelper.createAndPopulateMetadataTable(db, testMetadataTable, csvFile);

        assertThat(SQLiteUtils.doesTableExist(db, testMetadataTable), is(true));

        final String[] columnNames = {COLUMN_MD5_HASH};
        final String selectCriteria = CustomSQLiteQueryBuilder.formatCompareEquals(
                COLUMN_DATASET_FILENAME,
                CustomSQLiteQueryBuilder.quoteStringLiteral(SIMPLE_SEARCH_EXTERNAL_CSV_FILENAME));
        Cursor cursor = db.query(testMetadataTable, columnNames, selectCriteria, null, null, null, null);
        cursor.moveToFirst();
        String fileMd5 = cursor.getString(0);

        assertThat(fileMd5, is(Md5.getMd5Hash(csvFile)));
    }

    @Test
    public void doImport_reimportsCsvIfDatabaseFileIsDeleted() {
        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);
        assertThat(dbFile.exists(), is(true));

        dbFile.delete();

        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);
        assertThat(dbFile.exists(), is(true));
    }

    @Test
    public void doImport_reimportsCsvIfMetadataTableIsMissing() {
        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);
        assertThat(dbFile.exists(), is(true));

        // Remove the metadata table (mimicking prior versions without the metadata table)
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        SQLiteUtils.dropTable(db, EXTERNAL_METADATA_TABLE_NAME);
        db.close();

        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);
        assertThat(dbFile.exists(), is(true));
        db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        assertThat("metadata table should be recreated", SQLiteUtils.doesTableExist(db, EXTERNAL_METADATA_TABLE_NAME));
        db.close();
    }

    @Test
    public void doImport_reimportsCsvIfFileIsUpdated() throws IOException, InterruptedException {
        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);
        assertThat(dbFile.exists(), is(true));

        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        assertThat(db.rawQuery(SELECT_ALL_DATA_QUERY, null).getCount(), is(3));

        String originalHash = Md5.getMd5Hash(csvFile);
        String metadataTableHash = ExternalSQLiteOpenHelper.getLastMd5Hash(db, EXTERNAL_METADATA_TABLE_NAME, csvFile);
        assertThat(metadataTableHash, is(originalHash));

        try (Writer out = new BufferedWriter(new FileWriter(csvFile, true))) {
            out.write("\ncherimoya,Cherimoya");
        }

        String newHash = Md5.getMd5Hash(csvFile);
        assertThat(newHash, is(not(originalHash)));

        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);

        db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        assertThat(db.rawQuery(SELECT_ALL_DATA_QUERY, null).getCount(), is(4));

        // Check the metadata table import timestamp
        metadataTableHash = ExternalSQLiteOpenHelper.getLastMd5Hash(db, EXTERNAL_METADATA_TABLE_NAME, csvFile);
        assertThat(metadataTableHash, is(newHash));
    }

    @Test
    public void doImport_skipsImportIfFileNotUpdated() {
        // Create the DB file with an initial import
        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);
        assertThat(dbFile.exists(), is(true));
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.rawQuery(SELECT_ALL_DATA_QUERY, null);
        assertThat(cursor.getCount(), is(3));

        // Purge the contents of the data table before reimporting
        db.delete(EXTERNAL_DATA_TABLE_NAME, null, null);
        assertThat(SQLiteUtils.doesTableExist(db, EXTERNAL_DATA_TABLE_NAME), is(true));
        cursor = db.rawQuery(SELECT_ALL_DATA_QUERY, null);
        assertThat("expected zero rows of data after purging", cursor.getCount(), is(0));
        db.close();

        // Reimport
        externalDataReader = new ExternalDataReaderImpl(null);
        externalDataReader.doImport(formDefToCsvMedia);
        db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        cursor = db.rawQuery(SELECT_ALL_DATA_QUERY, null);
        assertThat("expected zero rows of data after reimporting unchanged file", cursor.getCount(), is(0));
    }

    private static Map<String, File> makeExternalDataMap() {
        Map<String, File> externalDataMap = new HashMap<>();
        externalDataMap.put(SIMPLE_SEARCH_EXTERNAL_CSV_NAME, csvFile);
        return externalDataMap;
    }
}
