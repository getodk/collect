/*
 * Copyright 2021 Smap Consulting
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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.google.gson.Gson;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.SmapReferenceDatabaseHelper;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.taskModel.ReferenceSurvey;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import androidx.loader.content.CursorLoader;

import static org.odk.collect.android.database.SmapReferenceDatabaseHelper.REF_SOURCE;
import static org.odk.collect.android.database.SmapReferenceDatabaseHelper.REF_SURVEY;

/**
 * This class is used to encapsulate all access to the references database
 */
public class SmapReferencesDao {

    SmapReferenceDatabaseHelper dbHelper;

    public SmapReferencesDao() {
        dbHelper = new SmapReferenceDatabaseHelper();
    }

    public void updateReferences(List<ReferenceSurvey> referenceSurveys) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String source = Utilities.getSource();

        // Delete existing references
        String selection = SmapReferenceDatabaseHelper.REF_SOURCE + " = ?";
        String[] selectionArgs = { source };

        db.delete(SmapReferenceDatabaseHelper.TABLE_NAME, selection, selectionArgs);

        for(ReferenceSurvey rs : referenceSurveys) {
            ContentValues values = new ContentValues();
            values.put(SmapReferenceDatabaseHelper.REF_SOURCE, source);
            values.put(SmapReferenceDatabaseHelper.REF_SURVEY, rs.survey);
            values.put(SmapReferenceDatabaseHelper.REF_REFERENCE_SURVEY, rs.referenceSurvey);
            values.put(SmapReferenceDatabaseHelper.REF_DATA_TABLE_NAME, rs.name);
            values.put(SmapReferenceDatabaseHelper.REF_COLUMN_NAMES, new Gson().toJson(rs.columns));
            values.put(SmapReferenceDatabaseHelper.REF_UPDATED_TIME, System.currentTimeMillis());

            db.insert(SmapReferenceDatabaseHelper.TABLE_NAME, null, values);
        }
    }
}
