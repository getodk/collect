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

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to encapsulate all access to the {@link org.odk.collect.android.provider.FormsProvider#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class FormsDao {

    public Cursor getFormsCursor() {
        return getFormsCursor(null, null, null, null);
    }

    public Cursor getFormsCursor(String sortOrder) {
        return getFormsCursor(null, null, null, sortOrder);
    }

    public Cursor getFormsCursor(String selection, String[] selectionArgs) {
        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Cursor getFormsCursorForFormId(String formId) {
        String selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=?";
        String selectionArgs[] = {formId};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Cursor getFormsCursorForFormFilePath(String formFIlePath) {
        String selection = FormsProviderAPI.FormsColumns.FORM_FILE_PATH + "=?";
        String selectionArgs[] = {formFIlePath};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Cursor getFormsCursorForMd5Hash(String md5Hash) {
        String selection = FormsProviderAPI.FormsColumns.MD5_HASH + "=?";
        String selectionArgs[] = {md5Hash};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Cursor getFormsCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public void deleteFormsDatabase() {
        Collect.getInstance().getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null);
    }
    
    private void deleteFormsFromIDs(String[] idsToDelete) {

        String selection = FormsProviderAPI.FormsColumns._ID + " in (";
        for (int i = 0; i < idsToDelete.length - 1; i++)
            selection += "?, ";
        selection += "? )";
        
        Collect.getInstance().getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, selection, idsToDelete);

    }

    public Uri saveForm(ContentValues values) {
        return Collect.getInstance().getContentResolver().insert(FormsProviderAPI.FormsColumns.CONTENT_URI, values);
    }

    public int updateForm(ContentValues values) {
        return updateForm(values, null, null);
    }

    public int updateForm(ContentValues values, String where, String[] whereArgs) {
        return Collect.getInstance().getContentResolver().update(FormsProviderAPI.FormsColumns.CONTENT_URI, values, where, whereArgs);
    }

    public List<Form> getFormsFromCursor(Cursor cursor) {
        List<Form> forms = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int displayNameColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME);
                    int descriptionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DESCRIPTION);
                    int jrFormIdColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
                    int jrVersionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION);
                    int formFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH);
                    int submissionUriColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.SUBMISSION_URI);
                    int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY);
                    int displaySubtextColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT);
                    int md5HashColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.MD5_HASH);
                    int dateColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DATE);
                    int jrCacheFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH);
                    int formMediaPathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH);
                    int languageColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.LANGUAGE);

                    Form form = new Form.Builder()
                            .displayName(cursor.getString(displayNameColumnIndex))
                            .description(cursor.getString(descriptionColumnIndex))
                            .jrFormId(cursor.getString(jrFormIdColumnIndex))
                            .jrVersion(cursor.getString(jrVersionColumnIndex))
                            .formFilePath(cursor.getString(formFilePathColumnIndex))
                            .submissionUri(cursor.getString(submissionUriColumnIndex))
                            .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
                            .displaySubtext(cursor.getString(displaySubtextColumnIndex))
                            .md5Hash(cursor.getString(md5HashColumnIndex))
                            .date(cursor.getLong(dateColumnIndex))
                            .jrCacheFilePath(cursor.getString(jrCacheFilePathColumnIndex))
                            .formMediaPath(cursor.getString(formMediaPathColumnIndex))
                            .language(cursor.getString(languageColumnIndex))
                            .build();

                    forms.add(form);
                }
            } finally {
                cursor.close();
            }
        }
        return forms;
    }

    public ContentValues getValuesFromFormObject(Form form) {
        ContentValues values = new ContentValues();
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, form.getDisplayName());
        values.put(FormsProviderAPI.FormsColumns.DESCRIPTION, form.getDescription());
        values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, form.getJrFormId());
        values.put(FormsProviderAPI.FormsColumns.JR_VERSION, form.getJrVersion());
        values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, form.getFormFilePath());
        values.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, form.getSubmissionUri());
        values.put(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT, form.getDisplaySubtext());
        values.put(FormsProviderAPI.FormsColumns.MD5_HASH, form.getMD5Hash());
        values.put(FormsProviderAPI.FormsColumns.DATE, form.getDate());
        values.put(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH, form.getJrCacheFilePath());
        values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, form.getFormMediaPath());
        values.put(FormsProviderAPI.FormsColumns.LANGUAGE, form.getLanguage());
        return values;
    }
}
