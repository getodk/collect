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
import android.provider.BaseColumns;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.FormsDatabaseHelper;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.storage.StoragePathProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to encapsulate all access to the {@link FormsDatabaseHelper#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class FormsDao {

    public Cursor getFormsCursor() {
        return getFormsCursor(null, null, null, null);
    }

    public Cursor getFormsCursor(String selection, String[] selectionArgs) {
        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Cursor getFormsCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver().query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor getFormsCursor(Uri uri) {
        return Collect.getInstance().getContentResolver().query(uri, null, null, null, null);
    }

    public Cursor getFormsCursorSortedByDateDesc(String formId, String formVersion) {
        String[] selectionArgs;
        String selection;

        if (formVersion == null) {
            selectionArgs = new String[]{formId};
            selection = FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsColumns.JR_VERSION + " IS NULL";
        } else {
            selectionArgs = new String[]{formId, formVersion};
            selection = FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsColumns.JR_VERSION + "=?";
        }

        // As long as we allow storing multiple forms with the same id and version number, choose
        // the newest one
        String order = FormsColumns.DATE + " DESC";

        return getFormsCursor(null, selection, selectionArgs, order);
    }

    public Cursor getFormsCursorForFormFilePath(String formFilePath) {
        String selection = FormsColumns.FORM_FILE_PATH + "=?";
        String[] selectionArgs = {new StoragePathProvider().getRelativeFormPath(formFilePath)};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Uri saveForm(ContentValues values) {
        return Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, values);
    }

    public int updateForm(ContentValues values, String where, String[] whereArgs) {
        return Collect.getInstance().getContentResolver().update(FormsColumns.CONTENT_URI, values, where, whereArgs);
    }

    /**
     * Returns all forms available through the cursor and closes the cursor.
     */
    public static List<Form> getFormsFromCursor(Cursor cursor) {
        List<Form> forms = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
                    int displayNameColumnIndex = cursor.getColumnIndex(FormsColumns.DISPLAY_NAME);
                    int descriptionColumnIndex = cursor.getColumnIndex(FormsColumns.DESCRIPTION);
                    int jrFormIdColumnIndex = cursor.getColumnIndex(FormsColumns.JR_FORM_ID);
                    int jrVersionColumnIndex = cursor.getColumnIndex(FormsColumns.JR_VERSION);
                    int formFilePathColumnIndex = cursor.getColumnIndex(FormsColumns.FORM_FILE_PATH);
                    int submissionUriColumnIndex = cursor.getColumnIndex(FormsColumns.SUBMISSION_URI);
                    int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(FormsColumns.BASE64_RSA_PUBLIC_KEY);
                    int md5HashColumnIndex = cursor.getColumnIndex(FormsColumns.MD5_HASH);
                    int dateColumnIndex = cursor.getColumnIndex(FormsColumns.DATE);
                    int jrCacheFilePathColumnIndex = cursor.getColumnIndex(FormsColumns.JRCACHE_FILE_PATH);
                    int formMediaPathColumnIndex = cursor.getColumnIndex(FormsColumns.FORM_MEDIA_PATH);
                    int languageColumnIndex = cursor.getColumnIndex(FormsColumns.LANGUAGE);
                    int autoSendColumnIndex = cursor.getColumnIndex(FormsColumns.AUTO_SEND);
                    int autoDeleteColumnIndex = cursor.getColumnIndex(FormsColumns.AUTO_DELETE);
                    int geometryXpathColumnIndex = cursor.getColumnIndex(FormsColumns.GEOMETRY_XPATH);
                    int deletedDateColumnIndex = cursor.getColumnIndex(FormsColumns.DELETED_DATE);

                    Form form = new Form.Builder()
                            .id(cursor.getLong(idColumnIndex))
                            .displayName(cursor.getString(displayNameColumnIndex))
                            .description(cursor.getString(descriptionColumnIndex))
                            .jrFormId(cursor.getString(jrFormIdColumnIndex))
                            .jrVersion(cursor.getString(jrVersionColumnIndex))
                            .formFilePath(new StoragePathProvider().getRelativeFormPath(cursor.getString(formFilePathColumnIndex)))
                            .submissionUri(cursor.getString(submissionUriColumnIndex))
                            .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
                            .md5Hash(cursor.getString(md5HashColumnIndex))
                            .date(cursor.getLong(dateColumnIndex))
                            .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(cursor.getString(jrCacheFilePathColumnIndex)))
                            .formMediaPath(new StoragePathProvider().getRelativeFormPath(cursor.getString(formMediaPathColumnIndex)))
                            .language(cursor.getString(languageColumnIndex))
                            .autoSend(cursor.getString(autoSendColumnIndex))
                            .autoDelete(cursor.getString(autoDeleteColumnIndex))
                            .geometryXpath(cursor.getString(geometryXpathColumnIndex))
                            .deleted(!cursor.isNull(deletedDateColumnIndex))
                            .build();

                    forms.add(form);
                }
            } finally {
                cursor.close();
            }
        }
        return forms;
    }
}
