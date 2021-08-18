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

import org.odk.collect.shared.strings.StringUtils;

import java.util.Arrays;
import java.util.List;

public class CustomSQLiteQueryBuilder {
    protected static final String SPACE = " ";
    protected static final String LIST_SEPARATOR = ", ";
    protected static final String SEMICOLON = ";";

    protected StringBuilder query;

    CustomSQLiteQueryBuilder() {
        query = new StringBuilder();
    }

    public void end() {}

    public String getQueryString() {
        return query.toString();
    }

    public static String quoteIdentifier(String unquoted) {
        return "\"" + unquoted + "\"";
    }

    public static String quoteStringLiteral(String unquoted) {
        return "\'" + unquoted + "\'";
    }

    public CustomSQLiteQueryBuilder select() {
        query.append("SELECT").append(SPACE);
        return this;
    }

    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    public CustomSQLiteQueryBuilder columnsForInsert(String... columns) {
        query.append('(');
        columnsForSelect(columns);
        query.append(')').append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder columnsForSelect(String... columns) {
        for (String column : columns) {
            query.append(column).append(',');
        }
        int lastCommaIndex = query.lastIndexOf(",");
        query.deleteCharAt(lastCommaIndex).append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder from(String table) {
        query.append("FROM").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder where(String selectCriteria) {
        query.append("WHERE").append(SPACE).append(selectCriteria).append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder where(String[] selectCriteria) {
        return where(formatLogicalAnd(selectCriteria));
    }

    public static String formatCompareEquals(String left, String right) {
        return left + " = " + right;
    }

    public static String formatLogicalAnd(String[] criteria) {
        return StringUtils.join(" AND ", Arrays.asList(criteria));
    }

    public CustomSQLiteQueryBuilder renameTable(String table) {
        query.append("ALTER TABLE").append(SPACE).append(table).append(SPACE).append("RENAME TO").append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder to(String table) {
        query.append(table);
        return this;
    }

    public CustomSQLiteQueryBuilder dropIfExists(String table) {
        query.append("DROP TABLE IF EXISTS").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder insertInto(String table) {
        query.append("INSERT INTO").append(SPACE).append(table);
        return this;
    }

    public CustomSQLiteQueryBuilder createTable(final String tableName) {
        query.append("CREATE").append(SPACE);
        return table(tableName);
    }

    public CustomSQLiteQueryBuilder columnsForCreate(List<String> columnDefinitions) {
        query.append('(').append(StringUtils.join(LIST_SEPARATOR, columnDefinitions)).append(')');
        return this;
    }

    public static String formatColumnDefinition(String columnName, String columnType) {
        return columnName + SPACE + columnType;
    }

    public CustomSQLiteQueryBuilder alter() {
        query.append("ALTER").append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder table(final String table) {
        query.append("TABLE").append(SPACE).append(table).append(SPACE);
        return this;
    }

    public CustomSQLiteQueryBuilder addColumn(final String columnName, final String columnType) {
        query.append("ADD COLUMN").append(SPACE).append(columnName).append(SPACE).append(columnType);
        return this;
    }
}
