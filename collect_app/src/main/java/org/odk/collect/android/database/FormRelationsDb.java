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
import java.util.HashSet;
import java.util.Set;

/**
 * Creates form relations database and provides useful CRUD functions
 *
 * The relations database consists of a single table. The contract for this
 * database is stored in `FormRelationsContract.java` in the same package.
 * All functions that create, read, update, or delete the records of this
 * database are here. Other modules should not access database tables
 * directly, but should instead go through this class.
 *
 * Creator: James K. Pringle
 * E-mail: jpringle@jhu.edu
 * Created: 20 August 2015
 * Last modified: 9 September 2015
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

    /**
     * Upgrading erases the old version and creates a fresh database.
     *
     * Nafundi's database had some typing errors and did not capture all the
     * information needed for managing form relations. Hence out with old and
     * in with the new.
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL(FormRelations.DELETE_TABLE);
        onCreate(db);
    }

    /**
     * A container class used in `getMappingsToParent`
     */
    public static class MappingData {
        public String parentNode;
        public String childNode;
    }

    /**
     * Returns all paired instance nodes between parent and child forms.
     *
     * Queries the database based on child instance id and returns the nodes
     * that are paired together with its parent instance. These nodes should
     * have the same information, i.e. when one is updated during a survey,
     * the other is updated programmatically. A mapping is defined using the
     * `saveInstance=/XPath/to/node` attribute and value in an XForm.
     *
     * @param childId The instance id of the child form.
     * @return Returns a list of parent node / child node pairs. Returns empty
     * if `childId` is not in the database.
     */
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

    /**
     * Gets the instance id of the parent form based on the child form.
     *
     * Queries the database and looks only at the first record. There should
     * be only one parent instance id per child instance id.
     *
     * @param childId The instance id of the child form.
     * @return Returns the instance id of the parent form. Returns -1 if no
     * form is associated with the `childId`.
     */
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

    /**
     * Gets the instance ids of all children forms in the database.
     *
     * Scans the child instance id column of the form relations table and
     * builds a set of all unique ids.
     *
     * @return Returns a set of all instance ids that are children forms.
     */
    public static Set<Long> getAllChildren() {
        Set<Long> allChildren = new HashSet<Long>();

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_CHILD_INSTANCE_ID
        };
        Cursor c = db.query(true, FormRelations.TABLE_NAME, projection, null, null,
                null, null, null, null);

        if ( null != c ) {
            while( c.moveToNext() ) {
                Long thisChild = c.getLong(c.getColumnIndex(
                        FormRelations.COLUMN_CHILD_INSTANCE_ID));
                allChildren.add(thisChild);
            }
            c.close();
        }

        db.close();
        return allChildren;
    }

    /**
     * Gets a child instance id based on the parent id and repeat index.
     *
     * Queries the database and looks only at the first record. There should
     * be only one child per parent instance id and repeat index.
     *
     * @param parentId The instance id of the parent form.
     * @param repeatIndex The repeat index in the parent form, greater than or
     *                    equal to 1.
     * @return Returns the instance id of the child form. Returns -1 if no
     * form is associated with the supplied parameters.
     */
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
                String.valueOf(parentId),
                String.valueOf(repeatIndex)
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

    /**
     * Gets the repeat index based on parent and child ids.
     *
     * Queries the database and looks only at the first record. There should
     * be only one repeat index per parent instance id and child instance id.
     *
     * @param parentId The instance id of the parent form.
     * @param childId The instance id of the child form.
     * @return Returns the repeat index of the parent form. Returns -1 if no
     * repeat index is found.
     */
    public static int getRepeatIndex(long parentId, long childId) {
        int repeatIndex = -1;

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_PARENT_INDEX
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? AND " +
                FormRelations.COLUMN_CHILD_INSTANCE_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(parentId),
                String.valueOf(childId)
        };

        Cursor c = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                repeatIndex = c.getInt(c.getColumnIndex(FormRelations.COLUMN_PARENT_INDEX));
            }
            c.close();
        }

        db.close();
        return repeatIndex;
    }

    /**
     * Gets the xpath for the repeatable where a child is created.
     *
     * Children forms are generally created inside a repeat construct within
     * the parent form. This method returns the xpath to the root of the
     * repeatable. This is useful for deleting the entire group/repeat that
     * created a child form.
     *
     * @param parentId The instance id of the parent form.
     * @param childId The instance id of the child form.
     * @return Returns the root node of the repeatable of the parent form.
     * Returns null if no repeatable is found or if the stored repeatables
     * are all null.
     */
    public static String getRepeatable(long parentId, long childId) {
        String repeatable = null;

        FormRelationsDb frdb = new FormRelationsDb();
        SQLiteDatabase db = frdb.getReadableDatabase();

        String[] projection = {
                FormRelations.COLUMN_REPEATABLE
        };
        String selection = FormRelations.COLUMN_PARENT_INSTANCE_ID + "=? AND " +
                FormRelations.COLUMN_CHILD_INSTANCE_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(parentId),
                String.valueOf(childId)
        };

        Cursor c = db.query(FormRelations.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                repeatable = c.getString(c.getColumnIndex(FormRelations.COLUMN_REPEATABLE));
                if ( repeatable != null ) {
                    break;
                }
            }
            if ( repeatable == null && c.getCount() > 0 ) {
                Log.w(TAG, "Searched for repeatable, and all entries are null: parentId (" +
                        parentId + ") and childId (" + childId + ")");
            }
            c.close();
        }

        if (LOCAL_LOG) {
            Log.d(TAG, "Found repeatable @" + repeatable + " for parentId (" + parentId +
                    ") and childId (" + childId + ")");
        }

        db.close();
        return repeatable;
    }

    /**
     * Gets instance ids of all children associated with a parent form.
     *
     * Queries the database based on parentId. Uses a set to keep only the
     * unique values from the returned records. Initially, it was thought that
     * the children ids should be sorted. However, that seems not to be the
     * case.
     *
     * @param parentId The instance id of the parent form.
     * @return Returns instance ids of all children related to supplied
     * parent. Returns an array of primitive long rather than a set.
     * Empty if no children are discovered.
     */
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

        Set<Long> ts = new HashSet<Long>();
        if (cursor != null) {
            while ( cursor.moveToNext() ) {
                long val = cursor.getLong(cursor.getColumnIndex(
                        FormRelations.COLUMN_CHILD_INSTANCE_ID));
                ts.add(val);
            }
            cursor.close();
        }
        db.close();

        // Convert set to array
        long[] childrenIds = new long[ts.size()];
        int i = 0;
        for (Long l : ts) {
            childrenIds[i] = l;
            i++;
        }

        return childrenIds;
    }

    /**
     * Deletes records where supplied instance id is in parent id column.
     *
     * When deleting an instance, it is easiest to remove all references to
     * that instance by scanning parent id and child id columns. Thus, the
     * supplied id may not necessarily be assumed to be for a parent form.
     *
     * @param instanceId Instance id
     * @return Returns the number of rows deleted from the database.
     */
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

    /**
     * Deletes records where supplied instance id is in child id column.
     *
     * When deleting an instance, it is easiest to remove all references to
     * that instance by scanning parent id and child id columns. Thus, the
     * supplied id may not necessarily be assumed to be for a child form.
     *
     * Only call this delete method if the child information (repeatable) IS
     * NOT removed from the parent form. In PMA terms, this is akin to
     * changing the age to outside the relevant range (15-49).
     *
     * @param instanceId Instance id
     * @return Returns the number of rows deleted from the database.
     */
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

    /**
     * Deletes the specified child in the db. Updates sibling db info.
     *
     * Removing a repeat shifts the index of all subsequent nodes down by one.
     * After deleting the child instance from the form relations table,
     * sibling forms with a repeat index greater than what is supplied have
     * node and repeatable information updated.
     *
     * Only call this method if the child information (repeatable) IS removed
     * from the parent form. In PMA terms, this is akin to removing a repeat
     * node (household member information) during the household survey.
     *
     * @param parentId The instance id of the parent form.
     * @param repeatIndex The repeat index in the parent form, greater than or
     *                    equal to 1.
     * @return Returns the number of rows deleted from the database.
     */
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
            Log.d(TAG, recordsDeleted + " records deleted");
        }

        if ( recordsDeleted > 0 ) {
            // Now must shift indices down that are greater than repeatIndex
            // so that sibling information is correct.
            String[] projection = {
                    FormRelations._ID,
                    FormRelations.COLUMN_PARENT_NODE,
                    FormRelations.COLUMN_PARENT_INDEX,
                    FormRelations.COLUMN_REPEATABLE
            };
            // According to https://www.sqlite.org/datatype3.html, section 3.3,
            // greater than (>) should be numeric, not string, comparison.
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

                    if (oldParentIndex == 1) {
                        Log.w(TAG, "Trying to shift index down on \'" + oldParentNode + "\'." +
                                " Index should be bigger than 1!");
                        continue;
                    }

                    int newParentIndex = oldParentIndex - 1;
                    String newParentNode = replaceIndex(oldParentNode, oldParentIndex,
                            newParentIndex);
                    String newParentRepeatable = replaceIndex(oldParentRepeatable, oldParentIndex,
                            newParentIndex);
                    ContentValues cv = new ContentValues();
                    cv.put(FormRelations.COLUMN_PARENT_NODE, newParentNode);
                    cv.put(FormRelations.COLUMN_PARENT_INDEX, newParentIndex);
                    cv.put(FormRelations.COLUMN_REPEATABLE, newParentRepeatable);
                    String updateSelection = FormRelations._ID + "=?";
                    String[] updateSelectionArgs = {
                            // Could this be an error? Instead, should get as
                            // long first, then convert to string?
                            cursor.getString(cursor.getColumnIndex(FormRelations._ID))
                    };
                    db.update(FormRelations.TABLE_NAME, cv, updateSelection, updateSelectionArgs);
                }
                cursor.close();
            }
        }

        db.close();
        return recordsDeleted;
    }

    /**
     * Replaces an index, i.e. [#], with another one in an xpath
     *
     * This is a helper method for updating xpaths. It replaces all
     * occurrences of [oldIndex] with [newIndex] in the supplied string.
     *
     * @param node The xpath to the node.
     * @param oldIndex The old index.
     * @param newIndex The new index, usually one less than old index.
     * @return Returns the xpath to the node with the correct replacements.
     */
    private static String replaceIndex(String node, int oldIndex, int newIndex) {
        String find = "[" + oldIndex + "]";
        String replace = "[" + newIndex + "]";
        String newNode = node.replace(find, replace);
        if (LOCAL_LOG) {
            Log.v(TAG, "Downshifting node from \'" + node + "\' to \'" + newNode + "\'");
        }
        return newNode;
    }

    /**
     * Inserts a row into the form relations database.
     *
     * For simplicity, all arguments should be strings (even though
     * `ContentValues.put()` can accept nearly all primitives). No checks
     * are performed for null values.
     *
     * @param parentId Parent instance id
     * @param parentNode Xpath for the node in the parent instance
     * @param repeatIndex Repeat index in the parent instance associated with
     *                    the child instance
     * @param childId Child instance id
     * @param childNode Xpath for the node in the child instance
     * @param repeatableNode Xpath for the root of the group/repeat
     * @return Returns the id (primary key) of the newly inserted row. This is
     * not generally useful.
     */
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

    /**
     * Determines if a record exists in the database.
     *
     * Each parameter must be supplied. No special treatment is given to null
     * arguments.
     *
     * @param parentId Parent instance id
     * @param parentNode Xpath for the node in the parent instance
     * @param repeatIndex Repeat index in the parent instance associated with
     *                    the child instance
     * @param childId Child instance id
     * @param childNode Xpath for the node in the child instance
     * @param repeatableNode Xpath for the root of the group/repeat
     * @return Returns true if and only if a matching row is discovered.
     */
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
