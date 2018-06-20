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

import static org.junit.Assert.assertEquals;

public class CustomSQLiteQueryBuilderTestCase {

    private final String[] columns = new String[] {"_id", "col1", "col2", "col3"};

    @Test
    public void selectTest() {
        assertEquals("SELECT ", new CustomSQLiteQueryBuilder().select().getQuery().toString());
    }

    @Test
    public void columnsForInsertTest() {
        assertEquals("(_id,col1,col2,col3 ) ", new CustomSQLiteQueryBuilder().columnsForInsert(columns).getQuery().toString());
    }

    @Test
    public void columnsForSelectTest() {
        assertEquals("_id,col1,col2,col3 ", new CustomSQLiteQueryBuilder().columnsForSelect(columns).getQuery().toString());
    }

    @Test
    public void fromTest() {
        assertEquals("FROM testTableName ", new CustomSQLiteQueryBuilder().from("testTableName").getQuery().toString());
    }

    @Test
    public void renameTableTest() {
        assertEquals("ALTER TABLE testTableName RENAME TO ", new CustomSQLiteQueryBuilder().renameTable("testTableName").getQuery().toString());
    }

    @Test
    public void toTest() {
        assertEquals("testTableName", new CustomSQLiteQueryBuilder().to("testTableName").getQuery().toString());
    }


    @Test
    public void dropIfExistsTest() {
        assertEquals("DROP TABLE IF EXISTS testTableName ", new CustomSQLiteQueryBuilder().dropIfExists("testTableName").getQuery().toString());
    }

    @Test
    public void insertIntoTest() {
        assertEquals("INSERT INTO testTableName", new CustomSQLiteQueryBuilder().insertInto("testTableName").getQuery().toString());
    }

    @Test
    public void alterTest() {
        assertEquals("ALTER ", new CustomSQLiteQueryBuilder().alter().getQuery().toString());
    }

    @Test
    public void tableTest() {
        assertEquals("TABLE testTableName ", new CustomSQLiteQueryBuilder().table("testTableName").getQuery().toString());
    }

    @Test
    public void addColumnTest() {
        assertEquals("ADD COLUMN Test text not null", new CustomSQLiteQueryBuilder().addColumn("Test", "text not null").getQuery().toString());
    }
}
