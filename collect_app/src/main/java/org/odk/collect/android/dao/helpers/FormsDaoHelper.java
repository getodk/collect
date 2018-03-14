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

import android.database.Cursor;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.provider.FormsProviderAPI;

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

    public static String getFormPath(String selection, String[] selectionArgs) {
        FormsDao formsDao = new FormsDao();
        String formPath = null;
        try (Cursor c = formsDao.getFormsCursor(selection, selectionArgs)) {
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                formPath = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH));
            }
        }
        return formPath;
    }

    public static String getFormLanguage(String formPath) {
        String newLanguage = "";
        try (Cursor c = new FormsDao().getFormsCursorForFormFilePath(formPath)) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                newLanguage = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns.LANGUAGE));
            }
        }
        return newLanguage;
    }
}
