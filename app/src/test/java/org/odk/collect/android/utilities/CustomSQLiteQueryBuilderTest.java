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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CustomSQLiteQueryBuilderTest {

    private final String[] columns = {"_id", "col1", "col2", "col3"};

    @Test
    public void quoteIdentifierTest() {
        assertEquals("\"identifier\"", CustomSQLiteQueryBuilder.quoteIdentifier("identifier"));
    }

    @Test
    public void quoteStringLiteral() {
        assertEquals("'literal'", CustomSQLiteQueryBuilder.quoteStringLiteral("literal"));
    }

    @Test
    public void selectTest() {
        assertEquals("SELECT ", new CustomSQLiteQueryBuilder().select().getQueryString());
    }

    @Test
    public void columnsForInsertTest() {
        assertEquals("(_id,col1,col2,col3 ) ", new CustomSQLiteQueryBuilder().columnsForInsert(columns).getQueryString());
    }

    @Test
    public void columnsForSelectTest() {
        assertEquals("_id,col1,col2,col3 ", new CustomSQLiteQueryBuilder().columnsForSelect(columns).getQueryString());
    }

    @Test
    public void fromTest() {
        assertEquals("FROM testTableName ", new CustomSQLiteQueryBuilder().from("testTableName").getQueryString());
    }

    @Test
    public void whereOneTest() {
        assertEquals("WHERE foo = bar ", new CustomSQLiteQueryBuilder().where("foo = bar").getQueryString());
    }

    @Test
    public void whereMultipleTest() {
        String[] criteria = {"foo = 1", "bar = 2"};
        assertEquals("WHERE foo = 1 AND bar = 2 ", new CustomSQLiteQueryBuilder().where(criteria).getQueryString());
    }

    @Test
    public void renameTableTest() {
        assertEquals("ALTER TABLE testTableName RENAME TO ", new CustomSQLiteQueryBuilder().renameTable("testTableName").getQueryString());
    }

    @Test
    public void toTest() {
        assertEquals("testTableName", new CustomSQLiteQueryBuilder().to("testTableName").getQueryString());
    }

    @Test
    public void dropIfExistsTest() {
        assertEquals("DROP TABLE IF EXISTS testTableName ", new CustomSQLiteQueryBuilder().dropIfExists("testTableName").getQueryString());
    }

    @Test
    public void insertIntoTest() {
        assertEquals("INSERT INTO testTableName", new CustomSQLiteQueryBuilder().insertInto("testTableName").getQueryString());
    }

    @Test
    public void alterTest() {
        assertEquals("ALTER ", new CustomSQLiteQueryBuilder().alter().getQueryString());
    }

    @Test
    public void tableTest() {
        assertEquals("TABLE testTableName ", new CustomSQLiteQueryBuilder().table("testTableName").getQueryString());
    }

    @Test
    public void addColumnTest() {
        assertEquals("ADD COLUMN Test text not null", new CustomSQLiteQueryBuilder().addColumn("Test", "text not null").getQueryString());
    }

    @Test
    public void createTableTest() {
        List<String> columnDefinitions = new ArrayList<>();
        columnDefinitions.add(CustomSQLiteQueryBuilder.formatColumnDefinition("col1", "TEXT"));
        columnDefinitions.add(CustomSQLiteQueryBuilder.formatColumnDefinition("col2", "INTEGER"));
        String actualQuery = new CustomSQLiteQueryBuilder().createTable("testTable").columnsForCreate(columnDefinitions).getQueryString();
        assertEquals("CREATE TABLE testTable (col1 TEXT, col2 INTEGER)", actualQuery);
    }
}
