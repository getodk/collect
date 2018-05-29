/*
 * Copyright 2018 Nafundi
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

package org.odk.collect.android.utilities;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class FormUtilsTest {

    @Test
    public void removeOldFormsTest() {
        MatrixCursor cursor = getCursor();
        cursor.addRow(new Object[] {1, "Form1", null, "form1", null, null, null, null, null, null, 1526806800000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {2, "Form1", null, "form1", null, null, null, null, null, null, 1526803200000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {3, "Form1", null, "form1", null, null, null, null, null, null, 1526799600000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {4, "Form1", null, "form1", null, null, null, null, null, null, 1526796000000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {5, "Form2", null, "form2", null, null, null, null, null, null, 1526796180000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {6, "Form3", null, "form3", null, null, null, null, null, null, 1526810400000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {7, "Form3", null, "form3", null, null, null, null, null, null, 1526814000000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {8, "Form4", null, "form4", null, null, null, null, null, null, 1526817600000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {9, "Form4", null, "form4", null, null, null, null, null, null, 1526817000000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {10, "Form4", null, "form4", null, null, null, null, null, null, 1526817300000L, null, null, null, null, null, null});

        Cursor filteredCursor = FormUtils.removeOldForms(cursor);

        assertNotNull(filteredCursor);
        assertEquals(4, filteredCursor.getCount());

        List<Form> forms = getFormsFromCursor(filteredCursor);

        assertEquals(4, forms.size());
        assertEquals(1, forms.get(0).getId());
        assertEquals(5, forms.get(1).getId());
        assertEquals(7, forms.get(2).getId());
        assertEquals(8, forms.get(3).getId());
    }

    private List<Form> getFormsFromCursor(Cursor cursor) {
        List<Form> forms = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
                int displayNameColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME);
                int jrFormIdColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
                int jrVersionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION);
                int dateColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DATE);

                Form form = new Form.Builder()
                        .id(cursor.getInt(idColumnIndex))
                        .displayName(cursor.getString(displayNameColumnIndex))
                        .jrFormId(cursor.getString(jrFormIdColumnIndex))
                        .jrVersion(cursor.getString(jrVersionColumnIndex))
                        .date(cursor.getLong(dateColumnIndex))
                        .build();

                forms.add(form);
            } while (cursor.moveToNext());
        }
        return forms;
    }

    private MatrixCursor getCursor() {
        return new MatrixCursor(
                new String[] {
                        BaseColumns._ID,
                        FormsProviderAPI.FormsColumns.DISPLAY_NAME,
                        FormsProviderAPI.FormsColumns.DESCRIPTION,
                        FormsProviderAPI.FormsColumns.JR_FORM_ID,
                        FormsProviderAPI.FormsColumns.JR_VERSION,
                        FormsProviderAPI.FormsColumns.FORM_FILE_PATH,
                        FormsProviderAPI.FormsColumns.SUBMISSION_URI,
                        FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY,
                        FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT,
                        FormsProviderAPI.FormsColumns.MD5_HASH,
                        FormsProviderAPI.FormsColumns.DATE,
                        FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH,
                        FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH,
                        FormsProviderAPI.FormsColumns.LANGUAGE,
                        FormsProviderAPI.FormsColumns.AUTO_SEND,
                        FormsProviderAPI.FormsColumns.AUTO_DELETE,
                        FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH}
        );
    }
}