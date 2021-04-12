package org.odk.collect.android.database.instances

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.forms.instances.Instance
import java.lang.Boolean

object DatabaseInstanceColumns : BaseColumns {

    // instance column names
    const val DISPLAY_NAME = "displayName"
    const val SUBMISSION_URI = "submissionUri"
    const val INSTANCE_FILE_PATH = "instanceFilePath"
    const val JR_FORM_ID = "jrFormId"
    const val JR_VERSION = "jrVersion"
    const val STATUS = "status"
    const val CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete"
    const val LAST_STATUS_CHANGE_DATE = "date"
    const val DELETED_DATE = "deletedDate"
    const val GEOMETRY = "geometry"
    const val GEOMETRY_TYPE = "geometryType"

    @JvmStatic
    fun getInstanceFromValues(values: ContentValues): Instance? {
        return Instance.Builder()
            .dbId(values.getAsLong(BaseColumns._ID))
            .displayName(values.getAsString(DISPLAY_NAME))
            .submissionUri(values.getAsString(SUBMISSION_URI))
            .canEditWhenComplete(Boolean.parseBoolean(values.getAsString(CAN_EDIT_WHEN_COMPLETE)))
            .instanceFilePath(values.getAsString(INSTANCE_FILE_PATH))
            .formId(values.getAsString(JR_FORM_ID))
            .formVersion(values.getAsString(JR_VERSION))
            .status(values.getAsString(STATUS))
            .lastStatusChangeDate(values.getAsLong(LAST_STATUS_CHANGE_DATE))
            .deletedDate(values.getAsLong(DELETED_DATE))
            .geometry(values.getAsString(GEOMETRY))
            .geometryType(values.getAsString(GEOMETRY_TYPE))
            .build()
    }

    @JvmStatic
    fun getInstanceFromCurrentCursorPosition(cursor: Cursor): Instance? {
        val dbId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
        val displayNameColumnIndex = cursor.getColumnIndex(DISPLAY_NAME)
        val submissionUriColumnIndex = cursor.getColumnIndex(SUBMISSION_URI)
        val canEditWhenCompleteIndex = cursor.getColumnIndex(CAN_EDIT_WHEN_COMPLETE)
        val instanceFilePathIndex = cursor.getColumnIndex(INSTANCE_FILE_PATH)
        val jrFormIdColumnIndex = cursor.getColumnIndex(JR_FORM_ID)
        val jrVersionColumnIndex = cursor.getColumnIndex(JR_VERSION)
        val statusColumnIndex = cursor.getColumnIndex(STATUS)
        val lastStatusChangeDateColumnIndex = cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)
        val deletedDateColumnIndex = cursor.getColumnIndex(DELETED_DATE)
        val geometryTypeColumnIndex = cursor.getColumnIndex(GEOMETRY_TYPE)
        val geometryColumnIndex = cursor.getColumnIndex(GEOMETRY)
        val databaseIdIndex = cursor.getColumnIndex(BaseColumns._ID)
        return Instance.Builder()
            .dbId(dbId)
            .displayName(cursor.getString(displayNameColumnIndex))
            .submissionUri(cursor.getString(submissionUriColumnIndex))
            .canEditWhenComplete(Boolean.valueOf(cursor.getString(canEditWhenCompleteIndex)))
            .instanceFilePath(StoragePathProvider().getAbsoluteInstanceFilePath(cursor.getString(instanceFilePathIndex)))
            .formId(cursor.getString(jrFormIdColumnIndex))
            .formVersion(cursor.getString(jrVersionColumnIndex))
            .status(cursor.getString(statusColumnIndex))
            .lastStatusChangeDate(cursor.getLong(lastStatusChangeDateColumnIndex))
            .deletedDate(if (cursor.isNull(deletedDateColumnIndex)) null else cursor.getLong(deletedDateColumnIndex))
            .geometryType(cursor.getString(geometryTypeColumnIndex))
            .geometry(cursor.getString(geometryColumnIndex))
            .dbId(cursor.getLong(databaseIdIndex))
            .build()
    }

    @JvmStatic
    fun getValuesFromInstance(instance: Instance): ContentValues? {
        val values = ContentValues()
        values.put(BaseColumns._ID, instance.dbId)
        values.put(DISPLAY_NAME, instance.displayName)
        values.put(SUBMISSION_URI, instance.submissionUri)
        values.put(CAN_EDIT_WHEN_COMPLETE, Boolean.toString(instance.canEditWhenComplete()))
        values.put(INSTANCE_FILE_PATH, StoragePathProvider().getRelativeInstancePath(instance.instanceFilePath))
        values.put(JR_FORM_ID, instance.formId)
        values.put(JR_VERSION, instance.formVersion)
        values.put(STATUS, instance.status)
        values.put(LAST_STATUS_CHANGE_DATE, instance.lastStatusChangeDate)
        values.put(DELETED_DATE, instance.deletedDate)
        values.put(GEOMETRY, instance.geometry)
        values.put(GEOMETRY_TYPE, instance.geometryType)
        return values
    }
}
