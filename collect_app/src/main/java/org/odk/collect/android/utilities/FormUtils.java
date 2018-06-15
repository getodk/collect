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

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;

import java.util.List;

import static android.provider.BaseColumns._ID;

public class FormUtils {

    private FormUtils() {
    }

    /**
     * Keep only newest form versions, that means if you have more than one form with given formId
     * only the newest one should be displayed.
     */
    public static Cursor removeOldForms(Cursor cursor) {
        List<Form> forms = new FormsDao().getFormsFromCursor(cursor);

        MatrixCursor filteredCursor = new MatrixCursor(
                new String[] {
                        _ID,
                        FormsProviderAPI.FormsColumns.DISPLAY_NAME,
                        FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT,
                        FormsProviderAPI.FormsColumns.JR_FORM_ID,
                        FormsProviderAPI.FormsColumns.JR_VERSION,
                        FormsProviderAPI.FormsColumns.DATE
                }
        );

        for (Form form : forms) {
            if (isThisFormTheNewestOne(forms, form)) {
                filteredCursor.addRow(new Object[] {
                        form.getId(),
                        form.getDisplayName(),
                        form.getDisplaySubtext(),
                        form.getJrFormId(),
                        form.getJrVersion(),
                        form.getDate()
                });
            }
        }

        return filteredCursor;
    }

    private static boolean isThisFormTheNewestOne(List<Form> forms, Form formForChecking) {
        for (Form form : forms) {
            if (form.getJrFormId().equals(formForChecking.getJrFormId()) && !form.equals(formForChecking)) {
                if (form.getDate() > formForChecking.getDate()) {
                    return false;
                }
            }
        }

        return true;
    }
}