package org.odk.collect.android.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLiteUtils {
    private SQLiteUtils() {
    }

    public static boolean doesColumnExist(SQLiteDatabase db, String tableName, String columnName) {
        return getColumnNames(db, tableName).contains(columnName);
    }

    public static List<String> getColumnNames(SQLiteDatabase db, String tableName) {
        String[] columnNames;
        try (Cursor c = db.query(tableName, null, null, null, null, null, null)) {
            columnNames = c.getColumnNames();
        }

        // Build a full-featured ArrayList rather than the limited array-backed List from asList
        return new ArrayList<>(Arrays.asList(columnNames));
    }
}
