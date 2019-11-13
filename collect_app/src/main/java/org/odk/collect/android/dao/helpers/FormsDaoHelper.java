/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.dao.helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.utilities.FileUtils;

import java.util.ArrayList;
import java.util.List;

public final class FormsDaoHelper {

    private FormsDaoHelper() {

    }

    public static int getFormsCount(String selection, String[] selectionArgs) {
        try (Cursor c = new FormsDao().getFormsCursor(selection, selectionArgs)) {
            if (c != null) {
                return c.getCount();
            }
        }

        throw new RuntimeException("Unable to get the forms count");
    }

    public static String getAbsoluteFormPath(String selection, String[] selectionArgs) {
        FormsDao formsDao = new FormsDao();
        String formPath = null;
        try (Cursor c = formsDao.getFormsCursor(selection, selectionArgs)) {
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                formPath = FileUtils.getAbsoluteFilePath(c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH)));
            }
        }
        return formPath;
    }

    public static String getFormLanguage(String formPath) {
        String newLanguage = "";
        try (Cursor c = new FormsDao().getFormsCursorForFormFilePath(formPath)) {
            if (c != null && c.getCount() == 1) {
                c.moveToFirst();
                newLanguage = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns.LANGUAGE));
            }
        }
        return newLanguage;
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

    public static ContentValues getValuesFromFormObject(Form form) {
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
