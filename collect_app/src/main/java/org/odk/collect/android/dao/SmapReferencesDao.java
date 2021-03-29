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
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.database.SmapReferenceDatabaseHelper;
import org.odk.collect.android.taskModel.LinkedSurvey;
import org.odk.collect.android.taskModel.ReferenceSurvey;
import org.odk.collect.android.utilities.Utilities;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to encapsulate all access to the references database
 */
public class SmapReferencesDao {

    SmapReferenceDatabaseHelper dbHelper;

    public SmapReferencesDao() {
        dbHelper = new SmapReferenceDatabaseHelper();
    }

    public void updateReferences(List<ReferenceSurvey> referenceSurveys) {

        if(referenceSurveys != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String source = Utilities.getSource();

            // Delete existing references
            String selection = SmapReferenceDatabaseHelper.REF_SOURCE + " = ?";
            String[] selectionArgs = {source};
            db.delete(SmapReferenceDatabaseHelper.TABLE_NAME, selection, selectionArgs);

            for (ReferenceSurvey rs : referenceSurveys) {
                ContentValues values = new ContentValues();
                values.put(SmapReferenceDatabaseHelper.REF_SOURCE, source);
                values.put(SmapReferenceDatabaseHelper.REF_SURVEY, rs.survey);
                values.put(SmapReferenceDatabaseHelper.REF_REFERENCE_SURVEY, rs.referenceSurvey);
                values.put(SmapReferenceDatabaseHelper.REF_DATA_TABLE_NAME, rs.tableName);
                values.put(SmapReferenceDatabaseHelper.REF_COLUMN_NAMES, new Gson().toJson(rs.columns));
                values.put(SmapReferenceDatabaseHelper.REF_UPDATED_TIME, System.currentTimeMillis());

                db.insert(SmapReferenceDatabaseHelper.TABLE_NAME, null, values);
            }
        }
    }

    public HashMap<String, LinkedSurvey> getLinkedSurveys(String survey) {

        HashMap<String, LinkedSurvey> surveys = null;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        String source = Utilities.getSource();

        String[] projection = {
                BaseColumns._ID,
                SmapReferenceDatabaseHelper.REF_REFERENCE_SURVEY,
                SmapReferenceDatabaseHelper.REF_DATA_TABLE_NAME,
                SmapReferenceDatabaseHelper.REF_COLUMN_NAMES
        };

        String selection = SmapReferenceDatabaseHelper.REF_SOURCE + " = ? and " + SmapReferenceDatabaseHelper.REF_SURVEY + " = ?";
        String[] selectionArgs = { source, survey };

        try {
            cursor = db.query(
                    SmapReferenceDatabaseHelper.TABLE_NAME,   // The table to query
                    projection,                               // The array of columns to return (pass null to get all)
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                            // don't group the rows
                    null,                             // don't filter by row groups
                    null                             // no sort
            );

            while (cursor.moveToNext()) {
                if(surveys == null) {
                    surveys = new HashMap<String, LinkedSurvey> ();
                }
                LinkedSurvey rs = new LinkedSurvey();
                rs.itemId = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
                rs.referenceSurvey = cursor.getString(cursor.getColumnIndexOrThrow(SmapReferenceDatabaseHelper.REF_REFERENCE_SURVEY));
                rs.tableName = cursor.getString(cursor.getColumnIndexOrThrow(SmapReferenceDatabaseHelper.REF_DATA_TABLE_NAME));

                String colString = cursor.getString(cursor.getColumnIndexOrThrow(SmapReferenceDatabaseHelper.REF_COLUMN_NAMES));
                if(colString != null && colString.trim().startsWith("{"));
                rs.columns = new Gson().fromJson(colString, new TypeToken<List<String>>() {}.getType());

                surveys.put(rs.referenceSurvey, rs);
            }

        } finally {
            cursor.close();
        }

        return surveys;
    }
}
