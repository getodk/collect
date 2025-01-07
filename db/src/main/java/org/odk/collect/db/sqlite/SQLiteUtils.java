package org.odk.collect.db.sqlite;

import static org.odk.collect.db.sqlite.SQLiteDatabaseExt.doesColumnExist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @deprecated use {@link SQLiteDatabaseExt} instead.
 */
@Deprecated
public final class SQLiteUtils {
    private SQLiteUtils() {
    }

    public static boolean doesTableExist(SQLiteDatabase db, String tableName) {
        final String sqliteSystemTable = CustomSQLiteQueryBuilder.quoteIdentifier("sqlite_master");
        final String nameColumn = CustomSQLiteQueryBuilder.quoteIdentifier("name");
        final String typeColumn = CustomSQLiteQueryBuilder.quoteIdentifier("type");
        final String tableLiteral = CustomSQLiteQueryBuilder.quoteStringLiteral("table");
        final String tableNameLiteral = CustomSQLiteQueryBuilder.quoteStringLiteral(tableName);

        final String[] columnsToSelect = {nameColumn};
        final String[] selectCriteria = {
                CustomSQLiteQueryBuilder.formatCompareEquals(typeColumn, tableLiteral),
                CustomSQLiteQueryBuilder.formatCompareEquals(nameColumn, tableNameLiteral)
        };

        Cursor cursor = db.query(sqliteSystemTable, columnsToSelect, CustomSQLiteQueryBuilder.formatLogicalAnd(selectCriteria), null, null, null, null);
        boolean foundTable = cursor.getCount() == 1;
        cursor.close();
        return foundTable;
    }

    public static void addColumn(SQLiteDatabase db, String table, String column, String type) {
        if (!doesColumnExist(db, table, column)) {
            CustomSQLiteQueryExecutor.begin(db)
                .alter().table(table).addColumn(column, type)
                .end();
        }
    }

    public static void renameTable(SQLiteDatabase db, String table, String newTable) {
        CustomSQLiteQueryExecutor.begin(db)
            .renameTable(table).to(newTable)
            .end();
    }

    public static void copyRows(SQLiteDatabase db, String srcTable, String[] columns, String dstTable) {
        CustomSQLiteQueryExecutor.begin(db)
            .insertInto(dstTable).columnsForInsert(columns)
                .select().columnsForSelect(columns).from(srcTable)
            .end();
    }

    public static void dropTable(SQLiteDatabase db, String table) {
        CustomSQLiteQueryExecutor.begin(db)
            .dropIfExists(table)
            .end();
    }
}
