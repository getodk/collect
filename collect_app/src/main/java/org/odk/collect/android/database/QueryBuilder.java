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

package org.odk.collect.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class QueryBuilder {
    private static final String SPACE = " ";
    private static final String SEMICOLON = ";";

    private final SQLiteDatabase db;

    private StringBuilder query;

    private QueryBuilder(SQLiteDatabase db) {
        this.db = db;
        query = new StringBuilder();
    }

    public static QueryBuilder begin(SQLiteDatabase db) {
        return new QueryBuilder(db);
    }

    public void end() throws SQLiteException {
        query.append(SEMICOLON);
        db.execSQL(query.toString());
    }

    public QueryBuilder select() {
        query.append("SELECT").append(SPACE);
        return this;
    }

    public QueryBuilder columnsForInsert(String... columns) {
        query.append("(");
        columnsForSelect(columns);
        query.append(")").append(SPACE);
        return this;
    }

    public QueryBuilder columnsForSelect(String... columns) {
        for (String column : columns) {
            query.append(column).append(",");
        }
        int lastCommaIndex = query.lastIndexOf(",");
        query.deleteCharAt(lastCommaIndex).append(SPACE);
        return this;
    }

    public QueryBuilder from(String table) {
        query.append("FROM").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public QueryBuilder renameTable(String table) {
        query.append("ALTER TABLE").append(SPACE).append(table).append(SPACE).append("RENAME TO").append(SPACE);
        return this;
    }

    public QueryBuilder dropIfExists(String table) {
        query.append("DROP TABLE IF EXISTS").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public QueryBuilder to(String table) {
        query.append(table);
        return this;
    }

    public QueryBuilder insertInto(String table) {
        query.append("INSERT INTO").append(SPACE).append(table);
        return this;
    }

    public QueryBuilder alter() {
        query.append("ALTER").append(SPACE);
        return this;
    }

    public QueryBuilder table(final String table) {
        query.append("TABLE").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public QueryBuilder defaultValue(final Object defaultValue) {
        query.append("DEFAULT").append(SPACE).append(defaultValue).append(SPACE);
        return this;
    }

    public QueryBuilder where() {
        query.append("WHERE").append(SPACE);
        return this;
    }

    public QueryBuilder column(final String column) {
        query.append(column).append(SPACE);
        return this;
    }

    public QueryBuilder isNull() {
        query.append("IS NULL").append(SPACE);
        return this;
    }
}