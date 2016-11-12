package org.odk.collect.android.provider;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Provides convenience methods for querying instances database.
 */

public class DatabaseReader {

    public Long[] getAllInstancesIds(Context context) {
        ArrayList<Long> result = new ArrayList<Long>();

        Cursor cursor = context.getContentResolver().query(
                InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null, null,
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");

        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            Long id = cursor.getLong(
                    cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
            result.add(id);

            cursor.moveToNext();
        }

        cursor.close();

        return result.toArray(new Long[result.size()]);
    }
}