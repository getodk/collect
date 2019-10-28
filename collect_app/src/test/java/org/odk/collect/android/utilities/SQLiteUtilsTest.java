package org.odk.collect.android.utilities;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.robolectric.RobolectricTestRunner;

import static android.content.Context.MODE_PRIVATE;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SQLiteUtilsTest {

    @Test
    public void doesColumnExistTest() {
        String tableName = "testTable";

        SQLiteDatabase db = Collect.getInstance().openOrCreateDatabase("testDatabase", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE " + tableName + " (id integer, column1 text);");

        assertTrue(SQLiteUtils.doesColumnExist(db, tableName, "id"));
        assertTrue(SQLiteUtils.doesColumnExist(db, tableName, "column1"));

        assertFalse(SQLiteUtils.doesColumnExist(db, tableName, "column2"));
    }
}
