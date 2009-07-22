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
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
        
    }
    
    

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public FileDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public FileDbAdapter open() throws SQLException {

        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String filename, String status) {
        ContentValues initialValues = new ContentValues();
        File f = new File(filename);
        initialValues.put(KEY_FILENAME, f.getName());
        initialValues.put(KEY_STATUS, status);
                                                  

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
   
    public boolean deleteNote(String filename) {

        return mDb.delete(DATABASE_TABLE, KEY_FILENAME + "='" + filename + "'", null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {
        cleanup();
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILENAME,
                KEY_STATUS}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_FILENAME, KEY_STATUS}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchNote(String filename) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_FILENAME, KEY_STATUS}, KEY_FILENAME + "='" + filename + "'", null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchNotes(String status) throws SQLException {
        cleanup();
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_FILENAME, KEY_STATUS}, KEY_STATUS + "='" + status + "'", null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_FILENAME, title);
        args.put(KEY_STATUS, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateNote(String path, String status) {
        ContentValues args = new ContentValues();
        args.put(KEY_STATUS, status);
        Log.e("Carl", "updating status for " + path + " with " + status);
        return mDb.update(DATABASE_TABLE, args, KEY_FILENAME + "='" + path + "'", null) > 0;
    }


    private void cleanup() {
        Cursor mCursor =
                mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILENAME, KEY_STATUS}, null,
                        null, null, null, null);
 //       if (mCursor != null) {
 //           mCursor.moveToFirst();
 //       }
        
        while (mCursor.moveToNext()) {
            String filename = mCursor.getString(mCursor.getColumnIndex(KEY_FILENAME));
            File f = new File(SharedConstants.ANSWERS_PATH + filename);
            if (!f.exists()) {
                deleteNote(filename);
            }
        }
        mCursor.close();
    }
}
