/*
 * Copyright (C) 2014 Smap Consulting Pty Ltd
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

package org.odk.collect.android.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.odk.collect.android.database.TraceProviderAPI.TraceColumns;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.utilities.STFileUtils;
import org.odk.collect.android.utilities.Utilities;

import java.util.List;

import timber.log.Timber;

public class TraceUtilities {


    public static void insertPoint(Location location) {

        Uri dbUri =  TraceColumns.CONTENT_URI;


        ContentValues values = new ContentValues();
        values.put(TraceColumns.LAT, location.getLatitude());
        values.put(TraceColumns.LON, location.getLongitude());
        values.put(TraceColumns.TIME, System.currentTimeMillis());

        values.put(TraceColumns.SOURCE, getSource());

        Collect.getInstance().getContentResolver().insert(dbUri, values);

    }

    private static String getSource() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(Collect.getInstance()
                        .getBaseContext());
        String serverUrl = settings.getString(
                GeneralKeys.KEY_SERVER_URL, null);
        return STFileUtils.getSource(serverUrl);
    }

    /*
     * Get the trail of points
     */
    public static long getPoints(List<PointEntry> entries, int limit, boolean desc) {

        String [] proj = {
                org.odk.collect.android.provider.TraceProviderAPI.TraceColumns._ID,
                org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.LAT,
                org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.LON,
                org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.TIME,
        };

        long id = 0;
        String [] selectArgs = {""};
        selectArgs[0] = Utilities.getSource();
        String selectClause = org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.SOURCE + " = ?";

        String sortOrder = org.odk.collect.android.provider.TraceProviderAPI.TraceColumns._ID + (desc ? " DESC" : " ASC") + " LIMIT " + limit + ";";

        final ContentResolver resolver = Collect.getInstance().getContentResolver();
        Cursor pointListCursor = resolver.query(org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.CONTENT_URI, proj, selectClause, selectArgs, sortOrder);


        if(pointListCursor != null) {

            pointListCursor.moveToFirst();
            boolean logged = false;
            while (!pointListCursor.isAfterLast()) {

                org.odk.collect.android.loaders.PointEntry entry = new PointEntry();

                entry.lat = pointListCursor.getDouble(pointListCursor.getColumnIndexOrThrow(org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.LAT));
                entry.lon = pointListCursor.getDouble(pointListCursor.getColumnIndexOrThrow(org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.LON));
                entry.time = pointListCursor.getLong(pointListCursor.getColumnIndexOrThrow(org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.TIME));

                id = pointListCursor.getLong(pointListCursor.getColumnIndexOrThrow(org.odk.collect.android.provider.TraceProviderAPI.TraceColumns._ID));

                if(!logged) {  // Hack to prevent the time being optimised away so it is not present in the class. Keep this! Important.
                    Timber.i("First Entry %f, %f, %d", entry.lat, entry.lon, entry.time);
                    logged = true;
                }
                entries.add(entry);
                pointListCursor.moveToNext();
            }
        }
        if(pointListCursor != null) {
            pointListCursor.close();
        }

        return id;
    }

    /*
     * Delete the trace points
     * If lastId is > 0 then only delete points up to and including this id
     */
    public static boolean deleteSource(long lastId) {

        Uri dbUri =  org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.CONTENT_URI;

        String [] selectArgsAll = {""};
        String [] selectArgsLimit = {"", ""};
        String [] selectArgs;


        String selectClauseAll = org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.SOURCE + " = ?";
        String selectClauseLimit = org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.SOURCE + " = ? and "
                + org.odk.collect.android.provider.TraceProviderAPI.TraceColumns._ID + " <= ?";
        String selectClause;

        if(lastId > 0) {
            selectClause = selectClauseLimit;
            selectArgs = selectArgsLimit;
            selectArgs[1] = String.valueOf(lastId);
        } else {
            selectClause = selectClauseAll;
            selectArgs = selectArgsAll;
        }
        selectArgs[0] = Utilities.getSource();

        boolean status;
        try {
            Collect.getInstance().getContentResolver().delete(dbUri, selectClause, selectArgs);
            status = true;
        } catch (Exception e) {
            status = false;
        }
        return status;

    }
}
