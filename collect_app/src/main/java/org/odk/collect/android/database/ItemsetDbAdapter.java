
package org.odk.collect.android.database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.odk.collect.android.application.Collect;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ItemsetDbAdapter {

    public static final String KEY_ID = "_id";

    private static final String TAG = "ItemsetDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "itemsets.db";
    private static final String DATABASE_TABLE = "itemset_";
    private static final int DATABASE_VERSION = 2;

    private static final String ITEMSET_TABLE = "itemsets";
    private static final String KEY_ITEMSET_HASH = "hash";
    private static final String KEY_PATH = "path";

    private static final String CREATE_ITEMSET_TABLE =
            "create table " + ITEMSET_TABLE + " (_id integer primary key autoincrement, "
                    + KEY_ITEMSET_HASH + " text, "
                    + KEY_PATH + " text "
                    + ");";

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {
        DatabaseHelper() {
            super(Collect.METADATA_PATH, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create table to keep track of the itemsets
            db.execSQL(CREATE_ITEMSET_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
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

    public ItemsetDbAdapter() {
    }

    /**
     * Open the database. If it cannot be opened, try to create a new instance
     * of the database. If it cannot be created, throw an exception to signal
     * the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ItemsetDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper();
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public boolean createTable(String formHash, String pathHash, String[] columns, String path) {
        StringBuilder sb = new StringBuilder();
        
        // get md5 of the path to itemset.csv, which is unique per form
        // the md5 is easier to use because it doesn't have chars like '/'
                
        sb.append("create table " + DATABASE_TABLE + pathHash
                + " (_id integer primary key autoincrement ");
        for (int j = 0; j < columns.length; j++) {
            // add double quotes in case the column is of label:lang
            sb.append(" , \"" + columns[j] + "\" text ");
            // create database with first line
        }
        sb.append(");");

        String tableCreate = sb.toString();
        Log.i(TAG, "create string: " + tableCreate);
        mDb.execSQL(tableCreate);

        ContentValues cv = new ContentValues();
        cv.put(KEY_ITEMSET_HASH, formHash);
        cv.put(KEY_PATH, path);
        mDb.insert(ITEMSET_TABLE, null, cv);

        return true;
    }

    public boolean addRow(String tableName, String[] columns, String[] newRow) {
        ContentValues cv = new ContentValues();

        // rows don't necessarily use all the columns
        // but a column is guaranteed to exist for a row (or else blow up)
        for (int i = 0; i < newRow.length; i++) {
            cv.put("\"" + columns[i] + "\"", newRow[i]);
        }
        mDb.insert(DATABASE_TABLE + tableName, null, cv);
        return true;
    }

    public boolean tableExists(String tableName) {
        // select name from sqlite_master where type = 'table'
        String selection = "type=? and name=?";
        String selectionArgs[] = {
                "table", DATABASE_TABLE + tableName
        };
        Cursor c = mDb.query("sqlite_master", null, selection, selectionArgs,
                null, null, null);
        boolean exists = false;
        if (c.getCount() == 1) {
            exists = true;
        }
        c.close();
        return exists;

    }

    public void beginTransaction() {
        mDb.execSQL("BEGIN");
    }

    public void commit() {
        mDb.execSQL("COMMIT");
    }

    public Cursor query(String hash, String selection, String[] selectionArgs) throws SQLException {
        Cursor mCursor = mDb.query(true, DATABASE_TABLE + hash, null, selection, selectionArgs,
                null, null, null, null);
        return mCursor;
    }

    public void dropTable(String pathHash, String path) {
        // drop the table
        mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + pathHash);

        // and remove the entry from the itemsets table
        String where = KEY_PATH + "=?";
        String[] whereArgs = {
            path
        };
        mDb.delete(ITEMSET_TABLE, where, whereArgs);
    }

    public Cursor getItemsets(String path) {
        String selection = KEY_PATH + "=?";
        String[] selectionArgs = {
            path
        };
        Cursor c = mDb.query(ITEMSET_TABLE, null, selection, selectionArgs, null, null, null);
        return c;
    }

    public void delete(String path) {
        Cursor c = getItemsets(path);
        if (c != null) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                String table = getMd5FromString(c.getString(c.getColumnIndex(KEY_PATH)));
                mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + table);
            }
            c.close();
        }

        String where = KEY_PATH + "=?";
        String[] whereArgs = {
            path
        };
        mDb.delete(ITEMSET_TABLE, where, whereArgs);
    }
    
    public static String getMd5FromString(String toEncode) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("MD5", e.getMessage());
        }
        md.update(toEncode.getBytes());
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        return hashtext;
    }

}
