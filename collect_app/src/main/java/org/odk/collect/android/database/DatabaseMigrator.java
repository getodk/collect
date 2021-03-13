package org.odk.collect.android.database;

import android.database.sqlite.SQLiteDatabase;

public interface DatabaseMigrator {
    void onCreate(SQLiteDatabase db);

    void onUpgrade(SQLiteDatabase db, int oldVersion);

    void onDowngrade(SQLiteDatabase db);
}