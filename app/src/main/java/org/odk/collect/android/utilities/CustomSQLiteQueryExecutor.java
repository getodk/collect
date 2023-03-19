package org.odk.collect.android.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public final class CustomSQLiteQueryExecutor extends CustomSQLiteQueryBuilder {
    private final SQLiteDatabase db;

    private CustomSQLiteQueryExecutor(SQLiteDatabase db) {
        super();
        this.db = db;
    }

    public static CustomSQLiteQueryExecutor begin(SQLiteDatabase db) {
        return new CustomSQLiteQueryExecutor(db);
    }

    @Override
    public void end() throws SQLiteException {
        query.append(SEMICOLON);
        db.execSQL(query.toString());
    }

    public Cursor query()  throws SQLiteException {
        query.append(SEMICOLON);
        return db.rawQuery(query.toString(), null);
    }
}
