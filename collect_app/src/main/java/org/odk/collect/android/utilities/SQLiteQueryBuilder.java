/*
 * Copyright 2017 Nafundi
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

package org.odk.collect.android.utilities;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class SQLiteQueryBuilder {
    private static final String SPACE = " ";
    private static final String SEMICOLON = ";";

    private final SQLiteDatabase db;

    private StringBuilder query;

    private SQLiteQueryBuilder(SQLiteDatabase db) {
        this.db = db;
        query = new StringBuilder();
    }

    public static SQLiteQueryBuilder begin(SQLiteDatabase db) {
        return new SQLiteQueryBuilder(db);
    }

    public void end() throws SQLiteException {
        query.append(SEMICOLON);
        db.execSQL(query.toString());
    }

    public SQLiteQueryBuilder select() {
        query.append("SELECT").append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder columnsForInsert(String... columns) {
        query.append("(");
        columnsForSelect(columns);
        query.append(")").append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder columnsForSelect(String... columns) {
        for (String column : columns) {
            query.append(column).append(",");
        }
        int lastCommaIndex = query.lastIndexOf(",");
        query.deleteCharAt(lastCommaIndex).append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder from(String table) {
        query.append("FROM").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder renameTable(String table) {
        query.append("ALTER TABLE").append(SPACE).append(table).append(SPACE).append("RENAME TO").append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder dropIfExists(String table) {
        query.append("DROP TABLE IF EXISTS").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder to(String table) {
        query.append(table);
        return this;
    }

    public SQLiteQueryBuilder insertInto(String table) {
        query.append("INSERT INTO").append(SPACE).append(table);
        return this;
    }

    public SQLiteQueryBuilder alter() {
        query.append("ALTER").append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder table(final String table) {
        query.append("TABLE").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder defaultValue(final Object defaultValue) {
        query.append("DEFAULT").append(SPACE).append(defaultValue).append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder where() {
        query.append("WHERE").append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder column(final String column) {
        query.append(column).append(SPACE);
        return this;
    }

    public SQLiteQueryBuilder isNull() {
        query.append("IS NULL").append(SPACE);
        return this;
    }
}