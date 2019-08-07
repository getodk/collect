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
import androidx.loader.content.CursorLoader;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to encapsulate all access to the {@link org.odk.collect.android.database.helpers.FormsDatabaseHelper#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class FormsDao {

    public Cursor getFormsCursor() {
        return getFormsCursor(null, null, null, null);
    }

    public Cursor getFormsCursor(String selection, String[] selectionArgs) {
        return getFormsCursor(null, selection, selectionArgs, null);
    }

    Cursor getFormsCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor getFormsCursor(String formId, String formVersion) {
        String[] selectionArgs;
        String selection;

        if (formVersion == null) {
            selectionArgs = new String[]{formId};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + " IS NULL";
        } else {
            selectionArgs = new String[]{formId, formVersion};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + "=?";
        }

        // As long as we allow storing multiple forms with the same id and version number, choose
        // the newest one
        String order = FormsProviderAPI.FormsColumns.DATE + " DESC";

        return getFormsCursor(null, selection, selectionArgs, order);
    }

    private CursorLoader getFormsCursorLoader(String sortOrder, boolean newestByFormId) {
        return getFormsCursorLoader(null, null, sortOrder, newestByFormId);
    }

    /**
     * Returns a loader filtered by the specified charSequence in the specified sortOrder. If
     * newestByFormId is true, only the most recently-downloaded version of each form is included.
     */
    public CursorLoader getFormsCursorLoader(CharSequence charSequence, String sortOrder, boolean newestByFormId) {
        CursorLoader cursorLoader;

        if (charSequence.length() == 0) {
            cursorLoader = getFormsCursorLoader(sortOrder, newestByFormId);
        } else {
            String selection = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = new String[]{"%" + charSequence + "%"};

            cursorLoader = getFormsCursorLoader(selection, selectionArgs, sortOrder, newestByFormId);
        }
        return cursorLoader;
    }

    public CursorLoader getFormsCursorLoader(CharSequence charSequence, String sortOrder) {
        return getFormsCursorLoader(charSequence, sortOrder, false);
    }

    /**
     * Builds and returns a new CursorLoader, passing on the configuration parameters. If
     * newestByFormID is true, only the most recently-downloaded version of each form is included.
     */
    private CursorLoader getFormsCursorLoader(String selection, String[] selectionArgs, String sortOrder, boolean newestByFormId) {
        Uri formUri = newestByFormId ? FormsProviderAPI.FormsColumns.CONTENT_NEWEST_FORMS_BY_FORMID_URI
                : FormsProviderAPI.FormsColumns.CONTENT_URI;

        return new CursorLoader(Collect.getInstance(), formUri, null, selection, selectionArgs, sortOrder);
    }

    public Cursor getFormsCursorForFormId(String formId) {
        String selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=?";
        String[] selectionArgs = {formId};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public String getFormTitleForFormIdAndFormVersion(String formId, String formVersion) {
        String formTitle = "";

        Cursor cursor = getFormsCursor(formId, formVersion);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    formTitle = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        return formTitle;
    }

    public boolean isFormEncrypted(String formId, String formVersion) {
        boolean encrypted = false;

        Cursor cursor = getFormsCursor(formId, formVersion);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY);
                    encrypted = cursor.getString(base64RSAPublicKeyColumnIndex) != null;
                }
            } finally {
                cursor.close();
            }
        }
        return encrypted;
    }

    public String getFormMediaPath(String formId, String formVersion) {
        String formMediaPath = null;

        Cursor cursor = getFormsCursor(formId, formVersion);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int formMediaPathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH);
                    formMediaPath = cursor.getString(formMediaPathColumnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return formMediaPath;
    }

    public Cursor getFormsCursorForFormFilePath(String formFilePath) {
        String selection = FormsProviderAPI.FormsColumns.FORM_FILE_PATH + "=?";
        String[] selectionArgs = {formFilePath};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Cursor getFormsCursorForMd5Hash(String md5Hash) {
        String selection = FormsProviderAPI.FormsColumns.MD5_HASH + "=?";
        String[] selectionArgs = {md5Hash};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public void deleteFormsDatabase() {
        Collect.getInstance().getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null);
    }

    public void deleteFormsFromIDs(String[] idsToDelete) {
        StringBuilder selection = new StringBuilder(FormsProviderAPI.FormsColumns._ID + " in (");
        for (int i = 0; i < idsToDelete.length - 1; i++) {
            selection.append("?, ");
        }
        selection.append("? )");

        //This will break if the number of forms to delete > SQLITE_MAX_VARIABLE_NUMBER (999)
        Collect.getInstance().getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, selection.toString(), idsToDelete);
    }

    public void deleteFormsFromMd5Hash(String... hashes) {
        List<String> idsToDelete = new ArrayList<>();
        Cursor c = null;
        try {
            for (String hash : hashes) {
                c = getFormsCursorForMd5Hash(hash);
                if (c != null && c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns._ID));
                    idsToDelete.add(id);
                    c.close();
                    c = null;
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        deleteFormsFromIDs(idsToDelete.toArray(new String[idsToDelete.size()]));
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

    /**
     * Returns all forms available through the cursor and closes the cursor.
     */
    public List<Form> getFormsFromCursor(Cursor cursor) {
        List<Form> forms = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
                    int displayNameColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME);
                    int descriptionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DESCRIPTION);
                    int jrFormIdColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
                    int jrVersionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION);
                    int formFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH);
                    int submissionUriColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.SUBMISSION_URI);
                    int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY);
                    int md5HashColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.MD5_HASH);
                    int dateColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DATE);
                    int jrCacheFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH);
                    int formMediaPathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH);
                    int languageColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.LANGUAGE);
                    int autoSendColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.AUTO_SEND);
                    int autoDeleteColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.AUTO_DELETE);
                    int lastDetectedFormVersionHashColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH);

                    Form form = new Form.Builder()
                            .id(cursor.getInt(idColumnIndex))
                            .displayName(cursor.getString(displayNameColumnIndex))
                            .description(cursor.getString(descriptionColumnIndex))
                            .jrFormId(cursor.getString(jrFormIdColumnIndex))
                            .jrVersion(cursor.getString(jrVersionColumnIndex))
                            .formFilePath(cursor.getString(formFilePathColumnIndex))
                            .submissionUri(cursor.getString(submissionUriColumnIndex))
                            .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
                            .md5Hash(cursor.getString(md5HashColumnIndex))
                            .date(cursor.getLong(dateColumnIndex))
                            .jrCacheFilePath(cursor.getString(jrCacheFilePathColumnIndex))
                            .formMediaPath(cursor.getString(formMediaPathColumnIndex))
                            .language(cursor.getString(languageColumnIndex))
                            .autoSend(cursor.getString(autoSendColumnIndex))
                            .autoDelete(cursor.getString(autoDeleteColumnIndex))
                            .lastDetectedFormVersionHash(cursor.getString(lastDetectedFormVersionHashColumnIndex))
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
        values.put(FormsProviderAPI.FormsColumns.MD5_HASH, form.getMD5Hash());
        values.put(FormsProviderAPI.FormsColumns.DATE, form.getDate());
        values.put(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH, form.getJrCacheFilePath());
        values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, form.getFormMediaPath());
        values.put(FormsProviderAPI.FormsColumns.LANGUAGE, form.getLanguage());
        return values;
    }
}
