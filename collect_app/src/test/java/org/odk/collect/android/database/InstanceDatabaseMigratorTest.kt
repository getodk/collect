package org.odk.collect.android.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns._ID
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.DELETED_DATE
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.DISPLAY_NAME
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY_TYPE
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.INSTANCE_FILE_PATH
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_FORM_ID
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_VERSION
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.STATUS
import org.odk.collect.android.database.instances.DatabaseInstanceColumns.SUBMISSION_URI
import org.odk.collect.android.database.instances.InstanceDatabaseMigrator

@RunWith(AndroidJUnit4::class)
class InstanceDatabaseMigratorTest {

    private var database = SQLiteDatabase.create(null)
    private var instancesDatabaseMigrator = InstanceDatabaseMigrator()

    @Before
    fun setup() {
        assertThat("Test expects different Instances DB version", DatabaseConstants.INSTANCES_DATABASE_VERSION, equalTo(7))
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun databaseIdsShouldNotBeReused() {
        instancesDatabaseMigrator.onCreate(database)
        val contentValues = getContentValuesForInstanceV6()

        database.insert(DatabaseConstants.INSTANCES_TABLE_NAME, null, contentValues)
        database.rawQuery("SELECT * FROM " + DatabaseConstants.INSTANCES_TABLE_NAME + ";", arrayOf<String>()).use { cursor ->
            assertThat(cursor.count, equalTo(1))
            cursor.moveToFirst()
            assertThat(cursor.getInt(cursor.getColumnIndex(_ID)), equalTo(1))
        }

        database.delete(DatabaseConstants.INSTANCES_TABLE_NAME, null, null)
        database.insert(DatabaseConstants.INSTANCES_TABLE_NAME, null, contentValues)

        database.rawQuery("SELECT * FROM " + DatabaseConstants.INSTANCES_TABLE_NAME + ";", arrayOf<String>()).use { cursor ->
            assertThat(cursor.count, equalTo(1))
            cursor.moveToFirst()
            assertThat(cursor.getInt(cursor.getColumnIndex(_ID)), equalTo(2))
        }
    }

    @Test
    fun onUpgrade_fromVersion6() {
        val oldVersion = 6
        database.version = oldVersion
        instancesDatabaseMigrator.createInstancesTableV6(database)

        val contentValues = getContentValuesForInstanceV6()

        database.insert(DatabaseConstants.INSTANCES_TABLE_NAME, null, contentValues)
        instancesDatabaseMigrator.onUpgrade(database, oldVersion)
        database.rawQuery("SELECT * FROM " + DatabaseConstants.INSTANCES_TABLE_NAME + ";", arrayOf<String>()).use { cursor ->
            assertThat(cursor.columnCount, equalTo(12))
            assertThat(cursor.count, equalTo(1))

            cursor.moveToFirst()

            assertThat(cursor.getInt(cursor.getColumnIndex(_ID)), equalTo(1))
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), equalTo(contentValues.getAsString(DISPLAY_NAME)))
            assertThat(cursor.getString(cursor.getColumnIndex(SUBMISSION_URI)), equalTo(contentValues.getAsString(SUBMISSION_URI)))
            assertThat(cursor.getString(cursor.getColumnIndex(CAN_EDIT_WHEN_COMPLETE)), equalTo(contentValues.getAsString(CAN_EDIT_WHEN_COMPLETE)))
            assertThat(cursor.getString(cursor.getColumnIndex(INSTANCE_FILE_PATH)), equalTo(contentValues.getAsString(INSTANCE_FILE_PATH)))
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), equalTo(contentValues.getAsString(JR_FORM_ID)))
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), equalTo(contentValues.getAsString(JR_VERSION)))
            assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), equalTo(contentValues.getAsString(STATUS)))
            assertThat(cursor.getInt(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), equalTo(contentValues.getAsInteger(LAST_STATUS_CHANGE_DATE)))
            assertThat(cursor.getInt(cursor.getColumnIndex(DELETED_DATE)), equalTo(contentValues.getAsInteger(DELETED_DATE)))
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY)), equalTo(contentValues.getAsString(GEOMETRY)))
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY_TYPE)), equalTo(contentValues.getAsString(GEOMETRY_TYPE)))
        }
    }

    private fun getContentValuesForInstanceV6(): ContentValues {
        return ContentValues().apply {
            put(DISPLAY_NAME, "DisplayName")
            put(SUBMISSION_URI, "SubmissionUri")
            put(CAN_EDIT_WHEN_COMPLETE, "True")
            put(INSTANCE_FILE_PATH, "InstanceFilePath")
            put(JR_FORM_ID, "JrFormId")
            put(JR_VERSION, "JrVersion")
            put(STATUS, "Status")
            put(LAST_STATUS_CHANGE_DATE, 0)
            put(DELETED_DATE, 0)
            put(GEOMETRY, "Geometry")
            put(GEOMETRY_TYPE, "GeometryType")
        }
    }
}
