package org.odk.collect.android;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FileDbAdapter {
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "FileDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;


    private static final String DATABASE_CREATE =
            "create table files (_id integer primary key autoincrement, "
                    + "filename text not null, status text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "files";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.e("Carl", "constructor datbase helper");
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.e("carl", "creating database");
            db.execSQL(DATABASE_CREATE);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS data");
            onCreate(db);
        }

    }

    
    public FileDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }


    public FileDbAdapter open() throws SQLException {

        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }


    public void close() {
        mDbHelper.close();
    }


    public long createFile(String filename, String status) {
        ContentValues initialValues = new ContentValues();
        File f = new File(filename);
        initialValues.put(KEY_FILENAME, f.getName());
        initialValues.put(KEY_STATUS, status);


        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }


    public boolean deleteFile(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }


    public boolean deleteFile(String filename) {

        return mDb.delete(DATABASE_TABLE, KEY_FILENAME + "='" + filename + "'", null) > 0;
    }


    public Cursor fetchAllFiles() {
        cleanup();
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILENAME, KEY_STATUS}, null,
                null, null, null, null);
    }


    public Cursor fetchFile(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILENAME, KEY_STATUS},
                        KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    public Cursor fetchFile(String filename) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILENAME, KEY_STATUS},
                        KEY_FILENAME + "='" + filename + "'", null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    public Cursor fetchFiles(String status) throws SQLException {
        cleanup();
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILENAME, KEY_STATUS},
                        KEY_STATUS + "='" + status + "'", null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    public boolean updateFile(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_FILENAME, title);
        args.put(KEY_STATUS, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }


    public boolean updateFile(String path, String status) {
        ContentValues args = new ContentValues();
        args.put(KEY_STATUS, status);
        Log.e("Carl", "updating status for " + path + " with " + status);
        return mDb.update(DATABASE_TABLE, args, KEY_FILENAME + "='" + path + "'", null) > 0;
    }


    private void cleanup() {
        Cursor mCursor =
                mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILENAME, KEY_STATUS}, null,
                        null, null, null, null);

        while (mCursor.moveToNext()) {
            String filename = mCursor.getString(mCursor.getColumnIndex(KEY_FILENAME));
            File f = new File(SharedConstants.ANSWERS_PATH + filename);
            if (!f.exists()) {
                deleteFile(filename);
            }
        }
        mCursor.close();
    }
}
