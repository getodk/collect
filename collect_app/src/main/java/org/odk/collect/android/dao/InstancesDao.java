/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.loader.content.CursorLoader;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DatabaseUtils;

import java.util.List;

/**
 * This class is used to encapsulate all access to the {@link org.odk.collect.android.provider.InstanceProvider#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class InstancesDao {

    public Cursor getSentInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " =? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getSentInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getSentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " =? and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader getSentInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " =? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public Cursor getUnsentInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " !=? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getUnsentInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " !=? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getUnsentInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getUnsentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " !=? and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public Cursor getSavedInstancesCursor(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL ";

        return getInstancesCursor(null, selection, null, sortOrder);
    }

    public CursorLoader getSavedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL ";

        return getInstancesCursorLoader(null, selection, null, sortOrder);
    }

    public CursorLoader getSavedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getSavedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {"%" + charSequence + "%"};
            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public Cursor getFinalizedInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + "=? or " + InstanceProviderAPI.InstanceColumns.STATUS + "=?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE, InstanceProviderAPI.STATUS_SUBMISSION_FAILED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getFinalizedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + "=? or " + InstanceProviderAPI.InstanceColumns.STATUS + "=?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE, InstanceProviderAPI.STATUS_SUBMISSION_FAILED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getFinalizedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getFinalizedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    "(" + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                            + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public Cursor getInstancesCursorForFilePath(String path) {
        String selection = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " LIKE ?";
        String[] selectionArgs = {"%" + DatabaseUtils.getRelativeFilePath(path)};

        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getAllCompletedUndeletedInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=?)";

        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getAllCompletedUndeletedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=?)";

        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                InstanceProviderAPI.STATUS_SUBMITTED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getCompletedUndeletedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getAllCompletedUndeletedInstancesCursorLoader(sortOrder);
        } else {
            String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                    + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";

            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }
        return cursorLoader;
    }

    public Cursor getInstancesCursorForId(String id) {
        String selection = InstanceProviderAPI.InstanceColumns._ID + "=?";
        String[] selectionArgs = {id};

        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getInstancesCursor() {
        return getInstancesCursor(null, null, null, null);
    }

    public Cursor getInstancesCursor(String selection, String[] selectionArgs) {
        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getInstancesCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver()
                .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getInstancesCursorLoader(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(
                Collect.getInstance(),
                InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    public Uri saveInstance(ContentValues values) {
        return Collect.getInstance().getContentResolver().insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
    }

    public int updateInstance(ContentValues values, String where, String[] whereArgs) {
        return Collect.getInstance().getContentResolver().update(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values, where, whereArgs);
    }

    public void deleteInstancesDatabase() {
        Collect.getInstance().getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null);
    }

    public void deleteInstances(List<String> absoluteInstanceFilePaths) {
        int count = absoluteInstanceFilePaths.size();
        int counter = 0;
        while (count > 0) {
            String[] selectionArgs = null;
            if (count > ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER) {
                selectionArgs = new String[
                        ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER];
            } else {
                selectionArgs = new String[count];
            }

            StringBuilder selection = new StringBuilder();
            selection.append(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
            int j = 0;
            while (j < selectionArgs.length) {
                selectionArgs[j] = "%" + absoluteInstanceFilePaths.get(
                        counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER + j);
                selection.append(" LIKE ?");

                if (j != selectionArgs.length - 1) {
                    selection.append(" OR " + InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
                }
                j++;
            }
            counter++;
            count -= selectionArgs.length;
            Collect.getInstance().getContentResolver()
                    .delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                            selection.toString(), selectionArgs);

        }
    }
}
