package org.odk.collect.android.fastexternalitemset;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.AltDatabasePathContext;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.Closeable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class ItemsetDbAdapter implements Closeable {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public static final String DATABASE_NAME = "itemsets.db";
    private static final String DATABASE_TABLE = "itemset_";
    private static final int DATABASE_VERSION = 2;

    private static final String ITEMSET_TABLE = "itemsets";
    public static final String KEY_ITEMSET_HASH = "hash";
    public static final String KEY_PATH = "path";

    private static final String CREATE_ITEMSET_TABLE =
            "CREATE TABLE IF NOT EXISTS " + ITEMSET_TABLE + " (_id integer primary key autoincrement, "
                    + KEY_ITEMSET_HASH + " text, "
                    + KEY_PATH + " text "
                    + ");";

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper() {
            super(new AltDatabasePathContext(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.METADATA), Collect.getInstance()), DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create table to keep track of the itemsets
            db.execSQL(CREATE_ITEMSET_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Timber.w("Upgrading database from version %d to %d, which will destroy all old data", oldVersion, newVersion);
            // first drop all of our generated itemset tables
            Cursor c = db.query(ITEMSET_TABLE, null, null, null, null, null, null);
            if (c != null) {
                c.move(-1);
                while (c.moveToNext()) {
                    String table = c.getString(c.getColumnIndex(KEY_ITEMSET_HASH));
                    db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + table);
                }
                c.close();
            }

            // then drop the table tracking itemsets itself
            db.execSQL("DROP TABLE IF EXISTS " + ITEMSET_TABLE);
            onCreate(db);
        }
    }

    /**
     * Open the database. If it cannot be opened, try to create a new instance
     * of the database. If it cannot be created, throw an exception to signal
     * the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ItemsetDbAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper();
        db = dbHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void close() {
        dbHelper.close();
    }

    public boolean createTable(String formHash, String pathHash, String[] columns, String path) {
        StringBuilder sb = new StringBuilder();

        // get md5 of the path to itemset.csv, which is unique per form
        // the md5 is easier to use because it doesn't have chars like '/'

        sb.append("create table ")
                .append(DATABASE_TABLE)
                .append(pathHash)
                .append(" (_id integer primary key autoincrement ");

        for (String column : columns) {
            if (!column.isEmpty()) {
                // add double quotes in case the column is of label:lang
                sb
                        .append(" , \"")
                        .append(column)
                        .append("\" text ");
                // create database with first line
            }
        }
        sb.append(");");

        String tableCreate = sb.toString();
        Timber.i("create string: %s", tableCreate);
        db.execSQL(tableCreate);

        ContentValues cv = new ContentValues();
        cv.put(KEY_ITEMSET_HASH, formHash);
        cv.put(KEY_PATH, new StoragePathProvider().getRelativeFormPath(path));
        db.insert(ITEMSET_TABLE, null, cv);

        return true;
    }

    public boolean addRow(String tableName, String[] columns, String[] newRow) {
        ContentValues cv = new ContentValues();

        // rows don't necessarily use all the columns
        // but a column is guaranteed to exist for a row (or else blow up)
        for (int i = 0; i < newRow.length; i++) {
            if (!columns[i].isEmpty()) {
                cv.put("\"" + columns[i] + "\"", newRow[i]);
            }
        }
        db.insert(DATABASE_TABLE + tableName, null, cv);
        return true;
    }

    public void beginTransaction() {
        db.execSQL("BEGIN");
    }

    public void commit() {
        db.execSQL("COMMIT");
    }

    public Cursor query(String hash, String selection, String[] selectionArgs) throws SQLException {
        return db.query(true, DATABASE_TABLE + hash, null, selection, selectionArgs,
                null, null, null, null);
    }

    public void dropTable(String pathHash, String path) {
        // drop the table
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + pathHash);

        // and remove the entry from the itemsets table
        String where = KEY_PATH + "=?";
        String[] whereArgs = {
                new StoragePathProvider().getRelativeFormPath(path)
        };
        db.delete(ITEMSET_TABLE, where, whereArgs);
    }

    public Cursor getItemsets(String path) {
        String selection = KEY_PATH + "=?";
        String[] selectionArgs = {
                new StoragePathProvider().getRelativeFormPath(path)
        };
        return db.query(ITEMSET_TABLE, null, selection, selectionArgs, null, null, null);
    }

    public Cursor getItemsets() {
        return db.query(ITEMSET_TABLE, null, null, null, null, null, null);
    }

    public void update(ContentValues values, String where, String[] whereArgs) {
        db.update(ITEMSET_TABLE, values, where, whereArgs);
    }

    public void delete(String path) {
        StoragePathProvider storagePathProvider = new StoragePathProvider();
        Cursor c = getItemsets(path);
        if (c != null) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                String table = getMd5FromString(storagePathProvider.getAbsoluteFormFilePath(c.getString(c.getColumnIndex(KEY_PATH))));
                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + table);
            }
            c.close();
        }

        String where = KEY_PATH + "=?";
        String[] whereArgs = {
                storagePathProvider.getRelativeFormPath(path)
        };
        db.delete(ITEMSET_TABLE, where, whereArgs);
    }

    public static String getMd5FromString(String toEncode) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e, "Unable to get MD5 algorithm due to : %s ", e.getMessage());
            return null;
        }

        md.update(toEncode.getBytes());
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }
}
