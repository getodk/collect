package org.odk.collect.db.sqlite;

import static android.content.Context.MODE_PRIVATE;
import static junit.framework.TestCase.assertTrue;

import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SQLiteUtilsTest {

    @Test
    public void doesTableExistTest() {
        final String tableName = "testTable";
        final String columnName = CustomSQLiteQueryBuilder.quoteIdentifier("col");

        SQLiteDatabase db = ApplicationProvider.getApplicationContext().openOrCreateDatabase("testDatabase", MODE_PRIVATE, null);

        TestCase.assertFalse(SQLiteUtils.doesTableExist(db, tableName));

        // Create the table and check again
        List<String> columnDefinitions = new ArrayList<>();
        columnDefinitions.add(CustomSQLiteQueryBuilder.formatColumnDefinition(columnName, "TEXT"));
        CustomSQLiteQueryExecutor.begin(db).createTable(tableName).columnsForCreate(columnDefinitions).end();

        assertTrue(SQLiteUtils.doesTableExist(db, tableName));
    }
}
