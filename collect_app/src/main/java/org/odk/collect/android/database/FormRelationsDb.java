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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.FormRelationsContract.FormRelations;

import java.util.ArrayList;

/**
 *  Implements database creation and upgrade for form relations.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 27 August 2015
 */
public class FormRelationsDb extends ODKSQLiteOpenHelper {

    private static final String TAG = "FormRelationsDb";
    private static final boolean LOCAL_LOG = true;

    public FormRelationsDb() {
        super(Collect.METADATA_PATH, FormRelationsContract.DATABASE_NAME, null,
                FormRelationsContract.DATABASE_VERSION);
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

    public static class MappingData {
        public String parentNode;
        public String childNode;
    }

    public static ArrayList<MappingData> getMappingsToParent(long childId) {
        ArrayList<MappingData> mappings = new ArrayList<MappingData>();

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_CHILD_NODE,
                FormRelations.COLUMN_PARENT_NODE
        };
        String selection = FormRelations.COLUMN_CHILD_INSTANCE_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(childId)
        };

        Cursor cursor = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);
        if (null != cursor) {
            while (cursor.moveToNext()) {
                MappingData thisMapping = new MappingData();
                thisMapping.childNode = cursor.getString(cursor.getColumnIndex(
                        FormRelations.COLUMN_CHILD_NODE));
                thisMapping.parentNode = cursor.getString(cursor.getColumnIndex(
                        FormRelations.COLUMN_PARENT_NODE));
                mappings.add(thisMapping);
            }
            cursor.close();
        }

        return mappings;
    }

    public static long getParent(long childId) {
        long found = -1;

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_PARENT_INSTANCE_ID
        };
        String selection = FormRelations.COLUMN_CHILD_INSTANCE_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(childId)
        };

        Cursor cursor = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);
        if (null != cursor) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                found = cursor.getLong(cursor.getColumnIndex(
                        FormRelations.COLUMN_PARENT_INSTANCE_ID));
            }
            cursor.close();
        }

        return found;
    }

    public static long getChild(long parentId, int repeatIndex) {
        long found = -1;

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_CHILD_INSTANCE_ID
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? and " +
                FormRelations.COLUMN_PARENT_INDEX + "=?";
        String[] selectionArgs = {
                String.valueOf(parentId), String.valueOf(repeatIndex)
        };

        Cursor cursor = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                found = cursor.getLong(cursor.getColumnIndex(
                        FormRelations.COLUMN_CHILD_INSTANCE_ID));
            }
            cursor.close();
        }

        db.close();
        return found;
    }

    public static int deleteChild(long instanceId) {
        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getWritableDatabase();

        String where = FormRelations.COLUMN_CHILD_INSTANCE_ID + "=?";
        String[] whereArgs = {
                String.valueOf(instanceId)
        };

        int recordsDeleted = db.delete(FormRelations.TABLE_NAME, where, whereArgs);
        db.close();
        return recordsDeleted;
    }

    public static int deleteChild(long parentId, int repeatIndex) {
        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getWritableDatabase();

        String where = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? and " +
                FormRelations.COLUMN_PARENT_INDEX + "=?";
        String[] whereArgs = {
                String.valueOf(parentId),
                String.valueOf(repeatIndex)
        };

        int recordsDeleted = db.delete(FormRelations.TABLE_NAME, where, whereArgs);
        db.close();
        return recordsDeleted;
    }

    public static long insert(String parentId, String parentNode, String repeatIndex,
                                 String childId, String childNode) {
        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(FormRelations.COLUMN_PARENT_INSTANCE_ID, parentId);
        cv.put(FormRelations.COLUMN_PARENT_NODE, parentNode);
        cv.put(FormRelations.COLUMN_PARENT_INDEX, repeatIndex);
        cv.put(FormRelations.COLUMN_CHILD_INSTANCE_ID, childId);
        cv.put(FormRelations.COLUMN_CHILD_NODE, childNode);
        long newRowId = db.insert(FormRelations.TABLE_NAME, null, cv);

        db.close();
        return newRowId;
    }

    public static boolean isRowExists(String parentId, String parentNode, String repeatIndex,
                                      String childId, String childNode) {
        boolean rowFound = false;

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations._ID
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? and " +
                FormRelations.COLUMN_PARENT_NODE + "=? and " +
                FormRelations.COLUMN_PARENT_INDEX + "=? and " +
                FormRelations.COLUMN_CHILD_INSTANCE_ID + "=? and " +
                FormRelations.COLUMN_CHILD_NODE + "=?";
        String[] selectionArgs = {
                parentId,
                parentNode,
                repeatIndex,
                childId,
                childNode
        };

        Cursor mCursor = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);
        if (mCursor != null) {
            if (mCursor.getCount() > 0) {
                rowFound = true;
            }
            mCursor.close();
        }

        db.close();
        return rowFound;
    }
}
