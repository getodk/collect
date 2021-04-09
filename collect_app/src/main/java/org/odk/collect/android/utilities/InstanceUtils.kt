package org.odk.collect.android.utilities

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import org.odk.collect.android.instances.Instance
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns
import org.odk.collect.android.storage.StoragePathProvider
import java.lang.Boolean

object InstanceUtils {

    @JvmStatic
    fun getInstanceFromValues(values: ContentValues): Instance? {
        return Instance.Builder()
            .dbId(values.getAsLong(BaseColumns._ID))
            .displayName(values.getAsString(InstanceColumns.DISPLAY_NAME))
            .submissionUri(values.getAsString(InstanceColumns.SUBMISSION_URI))
            .canEditWhenComplete(Boolean.parseBoolean(values.getAsString(InstanceColumns.CAN_EDIT_WHEN_COMPLETE)))
            .instanceFilePath(values.getAsString(InstanceColumns.INSTANCE_FILE_PATH))
            .formId(values.getAsString(InstanceColumns.JR_FORM_ID))
            .formVersion(values.getAsString(InstanceColumns.JR_VERSION))
            .status(values.getAsString(InstanceColumns.STATUS))
            .lastStatusChangeDate(values.getAsLong(InstanceColumns.LAST_STATUS_CHANGE_DATE))
            .deletedDate(values.getAsLong(InstanceColumns.DELETED_DATE))
            .geometry(values.getAsString(InstanceColumns.GEOMETRY))
            .geometryType(values.getAsString(InstanceColumns.GEOMETRY_TYPE))
            .build()
    }

    @JvmStatic
    fun getInstanceFromCurrentCursorPosition(cursor: Cursor): Instance? {
        val dbId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
        val displayNameColumnIndex = cursor.getColumnIndex(InstanceColumns.DISPLAY_NAME)
        val submissionUriColumnIndex = cursor.getColumnIndex(InstanceColumns.SUBMISSION_URI)
        val canEditWhenCompleteIndex = cursor.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE)
        val instanceFilePathIndex = cursor.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH)
        val jrFormIdColumnIndex = cursor.getColumnIndex(InstanceColumns.JR_FORM_ID)
        val jrVersionColumnIndex = cursor.getColumnIndex(InstanceColumns.JR_VERSION)
        val statusColumnIndex = cursor.getColumnIndex(InstanceColumns.STATUS)
        val lastStatusChangeDateColumnIndex = cursor.getColumnIndex(InstanceColumns.LAST_STATUS_CHANGE_DATE)
        val deletedDateColumnIndex = cursor.getColumnIndex(InstanceColumns.DELETED_DATE)
        val geometryTypeColumnIndex = cursor.getColumnIndex(InstanceColumns.GEOMETRY_TYPE)
        val geometryColumnIndex = cursor.getColumnIndex(InstanceColumns.GEOMETRY)
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
        values.put(InstanceColumns.DISPLAY_NAME, instance.displayName)
        values.put(InstanceColumns.SUBMISSION_URI, instance.submissionUri)
        values.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(instance.canEditWhenComplete()))
        values.put(InstanceColumns.INSTANCE_FILE_PATH, StoragePathProvider().getRelativeInstancePath(instance.instanceFilePath))
        values.put(InstanceColumns.JR_FORM_ID, instance.formId)
        values.put(InstanceColumns.JR_VERSION, instance.formVersion)
        values.put(InstanceColumns.STATUS, instance.status)
        values.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, instance.lastStatusChangeDate)
        values.put(InstanceColumns.DELETED_DATE, instance.deletedDate)
        values.put(InstanceColumns.GEOMETRY, instance.geometry)
        values.put(InstanceColumns.GEOMETRY_TYPE, instance.geometryType)
        return values
    }
}
