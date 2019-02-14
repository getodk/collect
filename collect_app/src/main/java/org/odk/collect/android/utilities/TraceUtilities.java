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

package org.odk.collect.android.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.TraceProviderAPI.TraceColumns;

import java.util.ArrayList;

public class TraceUtilities {

    /*
     * Get the trail of points
     */
    public static long getPoints(ArrayList<PointEntry> entries) {

        String [] proj = {
                TraceColumns._ID,
                TraceColumns.LAT,
                TraceColumns.LON,
                TraceColumns.TIME,
        };

        long id = 0;
        String [] selectArgs = {""};
        selectArgs[0] = Utilities.getSource();
        String selectClause = TraceColumns.SOURCE + " = ?";

        String sortOrder = TraceColumns._ID + " ASC LIMIT 10000; ";

        final ContentResolver resolver = Collect.getInstance().getContentResolver();
        Cursor pointListCursor = resolver.query(TraceColumns.CONTENT_URI, proj, selectClause, selectArgs, sortOrder);


        if(pointListCursor != null) {

            pointListCursor.moveToFirst();
            while (!pointListCursor.isAfterLast()) {

                PointEntry entry = new PointEntry();

                entry.lat = pointListCursor.getDouble(pointListCursor.getColumnIndex(TraceColumns.LAT));
                entry.lon = pointListCursor.getDouble(pointListCursor.getColumnIndex(TraceColumns.LON));
                entry.time = pointListCursor.getLong(pointListCursor.getColumnIndex(TraceColumns.TIME));

                id = pointListCursor.getLong(pointListCursor.getColumnIndex(TraceColumns._ID));

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

        Uri dbUri =  TraceColumns.CONTENT_URI;

        String [] selectArgs = {"", ""};
        selectArgs[0] = Utilities.getSource();

        String selectClauseAll = TraceColumns.SOURCE + " = ?";
        String selectClauseLimit = TraceColumns.SOURCE + " = ? and "
                + TraceColumns._ID + " <= ?";
        String selectClause = null;

        if(lastId > 0) {
            selectClause = selectClauseLimit;
            selectArgs[1] = String.valueOf(lastId);
        } else {
            selectClause = selectClauseAll;
        }

        boolean status;
        try {
            int count = Collect.getInstance().getContentResolver().delete(dbUri, selectClause, selectArgs);
            status = true;
        } catch (Exception e) {
            status = false;
        }
        return status;

    }

}
