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

    public static void addColumn(SQLiteDatabase db, String table, String column, String type) {
        if (!doesColumnExist(db, table, column)) {
            CustomSQLiteQueryBuilder.begin(db)
                .alter().table(table).addColumn(column, type)
                .end();
        }
    }

    public static void renameTable(SQLiteDatabase db, String table, String newTable) {
        CustomSQLiteQueryBuilder.begin(db)
            .renameTable(table).to(newTable)
            .end();
    }

    public static void copyRows(SQLiteDatabase db, String srcTable, String[] columns, String dstTable) {
        CustomSQLiteQueryBuilder.begin(db)
            .insertInto(dstTable).columnsForInsert(columns)
                .select().columnsForSelect(columns).from(srcTable)
            .end();
    }

    public static void dropTable(SQLiteDatabase db, String table) {
        CustomSQLiteQueryBuilder.begin(db)
            .dropIfExists(table)
            .end();
    }
}
