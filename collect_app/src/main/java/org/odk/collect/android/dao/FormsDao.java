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
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.storage.StoragePathProvider;

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

    public Cursor getFormsCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver().query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor getFormsCursor(Uri uri) {
        return Collect.getInstance().getContentResolver().query(uri, null, null, null, null);
    }

    public Cursor getFormsCursor(String formId, String formVersion) {
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
            String selection = FormsColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {"%" + charSequence + "%"};

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
        Uri formUri = newestByFormId ? FormsColumns.CONTENT_NEWEST_FORMS_BY_FORMID_URI
                : FormsColumns.CONTENT_URI;

        return new CursorLoader(Collect.getInstance(), formUri, null, selection, selectionArgs, sortOrder);
    }

    public Cursor getFormsCursorForFormId(String formId) {
        String selection = FormsColumns.JR_FORM_ID + "=?";
        String[] selectionArgs = {formId};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public String getFormTitleForFormIdAndFormVersion(String formId, String formVersion) {
        String formTitle = "";

        Cursor cursor = getFormsCursor(formId, formVersion);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    formTitle = cursor.getString(cursor.getColumnIndex(FormsColumns.DISPLAY_NAME));
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
                    int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(FormsColumns.BASE64_RSA_PUBLIC_KEY);
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
                    int formMediaPathColumnIndex = cursor.getColumnIndex(FormsColumns.FORM_MEDIA_PATH);
                    formMediaPath = new StoragePathProvider().getAbsoluteFormFilePath(cursor.getString(formMediaPathColumnIndex));
                }
            } finally {
                cursor.close();
            }
        }
        return formMediaPath;
    }

    public Cursor getFormsCursorForFormFilePath(String formFilePath) {
        String selection = FormsColumns.FORM_FILE_PATH + "=?";
        String[] selectionArgs = {new StoragePathProvider().getFormDbPath(formFilePath)};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public Cursor getFormsCursorForMd5Hash(String md5Hash) {
        String selection = FormsColumns.MD5_HASH + "=?";
        String[] selectionArgs = {md5Hash};

        return getFormsCursor(null, selection, selectionArgs, null);
    }

    public void deleteFormsDatabase() {
        Collect.getInstance().getContentResolver().delete(FormsColumns.CONTENT_URI, null, null);
    }

    public void deleteFormsFromIDs(String[] idsToDelete) {
        StringBuilder selection = new StringBuilder(FormsColumns._ID + " in (");
        for (int i = 0; i < idsToDelete.length - 1; i++) {
            selection.append("?, ");
        }
        selection.append("? )");

        //This will break if the number of forms to delete > SQLITE_MAX_VARIABLE_NUMBER (999)
        Collect.getInstance().getContentResolver().delete(FormsColumns.CONTENT_URI, selection.toString(), idsToDelete);
    }

    public void deleteFormsFromMd5Hash(String... hashes) {
        List<String> idsToDelete = new ArrayList<>();
        Cursor c = null;
        try {
            for (String hash : hashes) {
                c = getFormsCursorForMd5Hash(hash);
                if (c != null && c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndex(FormsColumns._ID));
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
        return Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, values);
    }

    public int updateForm(ContentValues values) {
        return updateForm(values, null, null);
    }

    public int updateForm(ContentValues values, String where, String[] whereArgs) {
        return Collect.getInstance().getContentResolver().update(FormsColumns.CONTENT_URI, values, where, whereArgs);
    }

    public int getCount() {
        try (Cursor c = getFormsCursor()) {
            return c.getCount();
        }
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
                    int lastDetectedFormVersionHashColumnIndex = cursor.getColumnIndex(FormsColumns.LAST_DETECTED_FORM_VERSION_HASH);
                    int geometryXpathColumnIndex = cursor.getColumnIndex(FormsColumns.GEOMETRY_XPATH);

                    Form form = new Form.Builder()
                            .id(cursor.getLong(idColumnIndex))
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
                            .geometryXpath(cursor.getString(geometryXpathColumnIndex))
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
        values.put(FormsColumns.DISPLAY_NAME, form.getDisplayName());
        values.put(FormsColumns.DESCRIPTION, form.getDescription());
        values.put(FormsColumns.JR_FORM_ID, form.getJrFormId());
        values.put(FormsColumns.JR_VERSION, form.getJrVersion());
        values.put(FormsColumns.FORM_FILE_PATH, form.getFormFilePath());
        values.put(FormsColumns.SUBMISSION_URI, form.getSubmissionUri());
        values.put(FormsColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        values.put(FormsColumns.MD5_HASH, form.getMD5Hash());
        values.put(FormsColumns.DATE, form.getDate());
        values.put(FormsColumns.JRCACHE_FILE_PATH, form.getJrCacheFilePath());
        values.put(FormsColumns.FORM_MEDIA_PATH, form.getFormMediaPath());
        values.put(FormsColumns.LANGUAGE, form.getLanguage());
        values.put(FormsColumns.AUTO_SEND, form.getAutoSend());
        values.put(FormsColumns.AUTO_DELETE, form.getAutoDelete());
        values.put(FormsColumns.GEOMETRY_XPATH, form.getGeometryXpath());

        return values;
    }
}
