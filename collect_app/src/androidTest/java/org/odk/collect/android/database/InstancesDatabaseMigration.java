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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.helpers.InstanceDatabaseHelper;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.List;

import static android.provider.BaseColumns._ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.provider.InstanceProvider.INSTANCES_TABLE_NAME;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE;

@RunWith(AndroidJUnit4.class)
public class InstancesDatabaseMigration {
    private static final String DATABASE_PATH = Collect.METADATA_PATH + "/" + InstanceDatabaseHelper.DATABASE_NAME;

    private SQLiteDatabase sqLiteDatabase;
    private InstancesDao instancesDao;

    @Before
    public void setUp() {
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH, null, null);
        instancesDao = new InstancesDao();
    }

    @Test
    public void testUpgradeFromVersion1ToVersion4() {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + INSTANCES_TABLE_NAME);

        sqLiteDatabase.setVersion(1);
        createInstancesTableForVersion1(sqLiteDatabase, INSTANCES_TABLE_NAME);
        insertExampleDataForInstanceTableVersion1(sqLiteDatabase);

        // Two tables because there is android_metadata table as well
        assertEquals(2, getTableCount(sqLiteDatabase));

        Cursor cursor = sqLiteDatabase.query(INSTANCES_TABLE_NAME, null, null, null, null, null, null);
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(8, cursor.getColumnCount());
        assertEquals(2, instances.size());
        assertTrue("Cascading Triple Select Form".equals(instances.get(0).getDisplayName()));
        assertTrue(null == instances.get(0).getSubmissionUri());
        assertTrue("/storage/emulated/0/odk/instances/Cascading Triple Select Form_2017-08-01_09-21-46/Cascading Triple Select Form_2017-08-01_09-21-46.xml".equals(instances.get(0).getInstanceFilePath()));
        assertTrue("CascadingTripleSelect".equals(instances.get(0).getJrFormId()));
        assertTrue("submitted".equals(instances.get(0).getStatus()));
        assertTrue(1501572116447L == instances.get(0).getLastStatusChangeDate());
        assertTrue("Sent on Tue, Aug 01, 2017 at 09:21".equals(instances.get(0).getDisplaySubtext()));

        new InstanceDatabaseHelper().onUpgrade(sqLiteDatabase, 1, 2);

        reopenDatabase();
        assertEquals(2, getTableCount(sqLiteDatabase));

        cursor = sqLiteDatabase.query(INSTANCES_TABLE_NAME, null, null, null, null, null, null);
        instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(11, cursor.getColumnCount());
        assertEquals(2, instances.size());
        assertTrue("Cascading Triple Select Form".equals(instances.get(0).getDisplayName()));
        assertTrue(null == instances.get(0).getSubmissionUri());
        assertTrue("true".equals(instances.get(0).getCanEditWhenComplete()));
        assertTrue("/storage/emulated/0/odk/instances/Cascading Triple Select Form_2017-08-01_09-21-46/Cascading Triple Select Form_2017-08-01_09-21-46.xml".equals(instances.get(0).getInstanceFilePath()));
        assertTrue("CascadingTripleSelect".equals(instances.get(0).getJrFormId()));
        assertTrue(null == instances.get(0).getJrVersion());
        assertTrue("submitted".equals(instances.get(0).getStatus()));
        assertTrue(1501572116447L == instances.get(0).getLastStatusChangeDate());
        assertTrue("Sent on Tue, Aug 01, 2017 at 09:21".equals(instances.get(0).getDisplaySubtext()));
        assertTrue(0 == instances.get(0).getDeletedDate());

        assertTrue("All widgets".equals(instances.get(1).getDisplayName()));
        assertTrue(null == instances.get(1).getSubmissionUri());
        assertTrue(null == instances.get(1).getCanEditWhenComplete());
        assertTrue("/storage/emulated/0/odk/instances/All widgets_2017-08-01_09-21-28/All widgets_2017-08-01_09-21-28.xml".equals(instances.get(1).getInstanceFilePath()));
        assertTrue("all-widgets".equals(instances.get(1).getJrFormId()));
        assertTrue(null == instances.get(1).getJrVersion());
        assertTrue("incomplete".equals(instances.get(1).getStatus()));
        assertTrue(1501572091388L == instances.get(1).getLastStatusChangeDate());
        assertTrue("Saved on Tue, Aug 01, 2017 at 09:21".equals(instances.get(1).getDisplaySubtext()));
        assertTrue(0 == instances.get(1).getDeletedDate());

        insertExampleDataForInstanceTableVersion4(sqLiteDatabase);
        cursor = sqLiteDatabase.query(INSTANCES_TABLE_NAME, null, null, null, null, null, null);
        instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(3, instances.size());

        assertTrue("Cascading Select Form".equals(instances.get(2).getDisplayName()));
        assertTrue(null == instances.get(2).getSubmissionUri());
        assertTrue("false".equals(instances.get(2).getCanEditWhenComplete()));
        assertTrue("/storage/emulated/0/odk/instances/Cascading Select Form_2017-08-01_09-20-43/Cascading Select Form_2017-08-01_09-20-43.xml".equals(instances.get(2).getInstanceFilePath()));
        assertTrue("CascadingSelect".equals(instances.get(2).getJrFormId()));
        assertTrue("2012072302".equals(instances.get(2).getJrVersion()));
        assertTrue("complete".equals(instances.get(2).getStatus()));
        assertTrue(1501572050597L == instances.get(2).getLastStatusChangeDate());
        assertTrue("Finalized on Tue, Aug 01, 2017 at 09:20".equals(instances.get(2).getDisplaySubtext()));
        assertTrue(1501573444639L == instances.get(2).getDeletedDate());
    }

    private void createInstancesTableForVersion1(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + InstanceProviderAPI.InstanceColumns.SUBMISSION_URI + " text, "
                + InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " text not null, "
                + InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " text not null, "
                + InstanceProviderAPI.InstanceColumns.STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null, "
                + InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT + " text not null);");
    }

    private void insertExampleDataForInstanceTableVersion1(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + INSTANCES_TABLE_NAME + " VALUES "
                + "(1, 'Cascading Triple Select Form', null, '/storage/emulated/0/odk/instances/Cascading Triple Select Form_2017-08-01_09-21-46/Cascading Triple Select Form_2017-08-01_09-21-46.xml', "
                + "'CascadingTripleSelect', 'submitted', 1501572116447, 'Sent on Tue, Aug 01, 2017 at 09:21');");

        db.execSQL("INSERT INTO " + INSTANCES_TABLE_NAME + " VALUES "
                + "(2, 'All widgets', null, '/storage/emulated/0/odk/instances/All widgets_2017-08-01_09-21-28/All widgets_2017-08-01_09-21-28.xml', "
                + "'all-widgets', 'incomplete', 1501572091388, 'Saved on Tue, Aug 01, 2017 at 09:21');");
    }

    private void insertExampleDataForInstanceTableVersion4(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + INSTANCES_TABLE_NAME + " VALUES "
                + "(3, 'Cascading Select Form', null, '/storage/emulated/0/odk/instances/Cascading Select Form_2017-08-01_09-20-43/Cascading Select Form_2017-08-01_09-20-43.xml',"
                + "'CascadingSelect', 'complete', 1501572050597, 'Finalized on Tue, Aug 01, 2017 at 09:20', 'false', '2012072302', 1501573444639);");
    }

    private int getTableCount(SQLiteDatabase sqLiteDatabase) {
        Cursor cursor = null;
        int tableCount = 0;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT count(name) FROM sqlite_master WHERE type = 'table' LIMIT 1", null);
            cursor.moveToFirst();
            int countColumnIndex = cursor.getColumnIndex("count(name)");
            tableCount = cursor.getInt(countColumnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tableCount;
    }

    private void reopenDatabase() {
        // Reopen database because sql queries are cached
        sqLiteDatabase.close();
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH, null, null);
    }
}