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
import java.util.TreeSet;

/**
 *  Implements database creation and upgrade for form relations.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Created: 20 August 2015
 *  Last modified: 2 September 2015
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
            Log.i(TAG, "onCreate. Created relations table.");
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

        db.close();
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

        db.close();
        return found;
    }

    public static long getChild(long parentId, int repeatIndex) {
        long found = -1;

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_CHILD_INSTANCE_ID
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? AND " +
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

    public static long[] getChildren(long parentId) {
        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_CHILD_INSTANCE_ID
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(parentId)
        };

        Cursor cursor = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);

        TreeSet<Long> ts = new TreeSet<Long>();
        if (cursor != null) {
            while ( cursor.moveToNext() ) {
                long val = cursor.getLong(cursor.getColumnIndex(
                        FormRelations.COLUMN_CHILD_INSTANCE_ID));
                ts.add(val);
            }
            cursor.close();
        }

        long[] childrenIds = new long[ts.size()];
        int i = 0;
        for (Long l : ts) {
            childrenIds[i] = l;
            i++;
        }

        db.close();
        return childrenIds;
    }

    public static int deleteAsParent(long instanceId) {
        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getWritableDatabase();

        String where = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=?";
        String[] whereArgs = {
                String.valueOf(instanceId)
        };

        int recordsDeleted = db.delete(FormRelations.TABLE_NAME, where, whereArgs);
        db.close();
        return recordsDeleted;
    }

    // Does not remove repeat, so no need to shift higher repeat indices
    public static int deleteAsChild(long instanceId) {
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

    // Called when repeat is removed
    public static int deleteChild(long parentId, int repeatIndex) {
        if (LOCAL_LOG) {
            Log.v(TAG, "Calling deleteChild(" + parentId + ", " + repeatIndex + ")");
        }
        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getWritableDatabase();

        String where = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? AND " +
                FormRelations.COLUMN_PARENT_INDEX + "=?";
        String[] whereArgs = {
                String.valueOf(parentId),
                String.valueOf(repeatIndex)
        };

        int recordsDeleted = db.delete(FormRelations.TABLE_NAME, where, whereArgs);

        if (LOCAL_LOG) {
            Log.v(TAG, recordsDeleted + " records deleted");
        }
        // Now must shift indices down that are greater than repeatIndex
        //
        // unclear if greater than (>) means numeric or string comparison
        // https://www.sqlite.org/datatype3.html, section 3.3 says it should be numeric
        String[] projection = {
                FormRelations._ID,
                FormRelations.COLUMN_PARENT_NODE,
                FormRelations.COLUMN_PARENT_INDEX,
                FormRelations.COLUMN_REPEATABLE
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? AND " +
                FormRelations.COLUMN_PARENT_INDEX + ">?";
        String[] selectionArgs = {
                String.valueOf(parentId),
                String.valueOf(repeatIndex)
        };
        Cursor cursor = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String oldParentNode = cursor.getString(cursor.getColumnIndex(
                        FormRelations.COLUMN_PARENT_NODE));
                int oldParentIndex = cursor.getInt(cursor.getColumnIndex(
                        FormRelations.COLUMN_PARENT_INDEX));
                String oldParentRepeatable = cursor.getString(cursor.getColumnIndex(
                        FormRelations.COLUMN_REPEATABLE));

                if ( oldParentIndex == 1 ) {
                    Log.w(TAG, "Trying to shift index down on \'" + oldParentNode + "\'." +
                            " Index should be bigger than 1!");
                    continue;
                }

                int newParentIndex = oldParentIndex - 1;
                String newParentNode = replaceIndex(oldParentNode, oldParentIndex, newParentIndex);
                String newParentRepeatable = replaceIndex(oldParentRepeatable, oldParentIndex, newParentIndex);
                ContentValues cv = new ContentValues();
                cv.put(FormRelations.COLUMN_PARENT_NODE, newParentNode);
                cv.put(FormRelations.COLUMN_PARENT_INDEX, newParentIndex);
                cv.put(FormRelations.COLUMN_REPEATABLE, newParentRepeatable);
                String updateSelection = FormRelations._ID + "=?";
                String[] updateSelectionArgs = {
                        // Could this be an error? Get as long first, then convert to string?
                        cursor.getString(cursor.getColumnIndex(FormRelations._ID))
                };
                db.update(FormRelations.TABLE_NAME, cv, updateSelection, updateSelectionArgs);
            }
            cursor.close();
        }

        db.close();
        return recordsDeleted;
    }

    private static String replaceIndex(String node, int oldIndex, int newIndex) {
        String find = "[" + oldIndex + "]";
        String replace = "[" + newIndex + "]";
        String newNode = node.replace(find, replace);
        if (LOCAL_LOG) {
            Log.v(TAG, "Downshifting node from \'" + node + "\' to \'" + newNode + "\'");
        }
        return newNode;
    }

    public static long insert(String parentId, String parentNode, String repeatIndex,
                                 String childId, String childNode, String repeatableNode) {
        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(FormRelations.COLUMN_PARENT_INSTANCE_ID, parentId);
        cv.put(FormRelations.COLUMN_PARENT_NODE, parentNode);
        cv.put(FormRelations.COLUMN_PARENT_INDEX, repeatIndex);
        cv.put(FormRelations.COLUMN_CHILD_INSTANCE_ID, childId);
        cv.put(FormRelations.COLUMN_CHILD_NODE, childNode);
        cv.put(FormRelations.COLUMN_REPEATABLE, repeatableNode);
        long newRowId = db.insert(FormRelations.TABLE_NAME, null, cv);

        db.close();
        return newRowId;
    }

    public static boolean isRowExists(String parentId, String parentNode, String repeatIndex,
                                      String childId, String childNode, String repeatableNode) {
        boolean rowFound = false;

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations._ID
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? AND " +
                FormRelations.COLUMN_PARENT_NODE + "=? AND " +
                FormRelations.COLUMN_PARENT_INDEX + "=? AND " +
                FormRelations.COLUMN_CHILD_INSTANCE_ID + "=? AND " +
                FormRelations.COLUMN_CHILD_NODE + "=? AND " +
                FormRelations.COLUMN_REPEATABLE + "=?";
        String[] selectionArgs = {
                parentId,
                parentNode,
                repeatIndex,
                childId,
                childNode,
                repeatableNode
        };

        Cursor cursor = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                rowFound = true;
            }
            cursor.close();
        }

        db.close();
        return rowFound;
    }
}
