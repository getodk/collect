package org.odk.collect.android.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteUtils {
    private SQLiteUtils() {
    }

    public static boolean doesColumnExist(SQLiteDatabase db, String tableName, String columnName) {
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null)) {
            if (cursor != null) {
                return cursor.getColumnIndex(columnName) != -1;
            }
        }
        return false;
    }
}
