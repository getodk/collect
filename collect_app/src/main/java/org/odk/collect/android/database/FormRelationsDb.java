/* The MIT License (MIT)
 *
 *       Copyright (c) 2015 PMA2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.odk.collect.android.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.FormRelationsContract.FormRelations;

/**
 *  Implements database creation and upgrade for form relations.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 25 August 2015
 */
public class FormRelationsDb extends ODKSQLiteOpenHelper {

    private static final String TAG = "FormRelationsDb";
    private static final boolean LOCAL_LOG = true;

    public FormRelationsDb() {
        super(Collect.METADATA_PATH, FormRelationsContract.DATABASE_NAME, null,
                FormRelationsContract.DATABASE_VERSION);
        if (LOCAL_LOG) {
            Log.d(TAG, "constructor");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (LOCAL_LOG) {
            Log.d(TAG, "onCreate. Created relations table.");
        }
        db.execSQL(FormRelations.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL(FormRelations.DELETE_TABLE);
        onCreate(db);
    }

    /*
     *  public void insertSomething(something) {
     *      SQLiteDatabase db = this.getWritableDatabase();
     *      //do stuff
     *      db.close();
     */


    public static long getChild(long parentId, int repeatIndex) {
        long mFound = -1;

        FormRelationsDb mFrdb = new FormRelationsDb();
        SQLiteDatabase mDb = mFrdb.getReadableDatabase();

        String[] columns = {
                FormRelations.COLUMN_CHILD_INSTANCE_ID
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? and " +
                FormRelations.COLUMN_PARENT_NODE + "=?";
        String[] selectionArgs = {
                String.valueOf(parentId), String.valueOf(repeatIndex)
        };
        /* // Old way
        Cursor mCursor = mDb.query(true, FormRelations.TABLE_NAME, null, selection, selectionArgs, null,
                null, null, null);
        */
        Cursor mCursor = mDb.query(FormRelations.TABLE_NAME, columns, selection, selectionArgs,
                null, null, null);
        if (mCursor != null) {
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                mFound = mCursor.getLong(mCursor.getColumnIndex(
                        FormRelations.COLUMN_CHILD_INSTANCE_ID));
            }
            mCursor.close();
        }

        mDb.close();

        return mFound;
    }
}
