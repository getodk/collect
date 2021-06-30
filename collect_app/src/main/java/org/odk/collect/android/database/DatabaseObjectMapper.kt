package org.odk.collect.android.database

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import org.odk.collect.android.database.forms.DatabaseFormColumns
import org.odk.collect.android.database.instances.DatabaseInstanceColumns
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.PathUtils.getAbsoluteFilePath
import org.odk.collect.shared.PathUtils.getRelativeFilePath
import java.lang.Boolean

object DatabaseObjectMapper {

    @JvmStatic
    fun getValuesFromForm(form: Form, formsPath: String): ContentValues {
        val formFilePath = getRelativeFilePath(formsPath, form.formFilePath)
        val formMediaPath = form.formMediaPath?.let { getRelativeFilePath(formsPath, it) }

        val values = ContentValues()
        values.put(BaseColumns._ID, form.dbId)
        values.put(DatabaseFormColumns.DISPLAY_NAME, form.displayName)
        values.put(DatabaseFormColumns.DESCRIPTION, form.description)
        values.put(DatabaseFormColumns.JR_FORM_ID, form.formId)
        values.put(DatabaseFormColumns.JR_VERSION, form.version)
        values.put(DatabaseFormColumns.FORM_FILE_PATH, formFilePath)
        values.put(DatabaseFormColumns.SUBMISSION_URI, form.submissionUri)
        values.put(DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY, form.basE64RSAPublicKey)
        values.put(DatabaseFormColumns.MD5_HASH, form.mD5Hash)
        values.put(DatabaseFormColumns.FORM_MEDIA_PATH, formMediaPath)
        values.put(DatabaseFormColumns.LANGUAGE, form.language)
        values.put(DatabaseFormColumns.AUTO_SEND, form.autoSend)
        values.put(DatabaseFormColumns.AUTO_DELETE, form.autoDelete)
        values.put(DatabaseFormColumns.GEOMETRY_XPATH, form.geometryXpath)
        return values
    }

    @JvmStatic
    fun getFormFromValues(values: ContentValues, formsPath: String, cachePath: String): Form {
        val formFilePath = getAbsoluteFilePath(
            formsPath,
            values.getAsString(DatabaseFormColumns.FORM_FILE_PATH)
        )

        val cacheFilePath = values.getAsString(DatabaseFormColumns.JRCACHE_FILE_PATH)?.let {
            getAbsoluteFilePath(cachePath, it)
        }

        val mediaPath = values.getAsString(DatabaseFormColumns.FORM_MEDIA_PATH)?.let {
            getAbsoluteFilePath(formsPath, it)
        }

        return Form.Builder()
            .dbId(values.getAsLong(BaseColumns._ID))
            .displayName(values.getAsString(DatabaseFormColumns.DISPLAY_NAME))
            .description(values.getAsString(DatabaseFormColumns.DESCRIPTION))
            .formId(values.getAsString(DatabaseFormColumns.JR_FORM_ID))
            .version(values.getAsString(DatabaseFormColumns.JR_VERSION))
            .formFilePath(formFilePath)
            .submissionUri(values.getAsString(DatabaseFormColumns.SUBMISSION_URI))
            .base64RSAPublicKey(values.getAsString(DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY))
            .md5Hash(values.getAsString(DatabaseFormColumns.MD5_HASH))
            .date(values.getAsLong(DatabaseFormColumns.DATE))
            .jrCacheFilePath(cacheFilePath)
            .formMediaPath(mediaPath)
            .language(values.getAsString(DatabaseFormColumns.LANGUAGE))
            .autoSend(values.getAsString(DatabaseFormColumns.AUTO_SEND))
            .autoDelete(values.getAsString(DatabaseFormColumns.AUTO_DELETE))
            .geometryXpath(values.getAsString(DatabaseFormColumns.GEOMETRY_XPATH))
            .deleted(values.getAsLong(DatabaseFormColumns.DELETED_DATE) != null)
            .build()
    }

    @JvmStatic
    fun getFormFromCurrentCursorPosition(
        cursor: Cursor,
        formsPath: String,
        cachePath: String
    ): Form? {
        val idColumnIndex = cursor.getColumnIndex(BaseColumns._ID)
        val displayNameColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DISPLAY_NAME)
        val descriptionColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DESCRIPTION)
        val jrFormIdColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.JR_FORM_ID)
        val jrVersionColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.JR_VERSION)
        val formFilePathColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.FORM_FILE_PATH)
        val submissionUriColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.SUBMISSION_URI)
        val base64RSAPublicKeyColumnIndex =
            cursor.getColumnIndex(DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY)
        val md5HashColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.MD5_HASH)
        val dateColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DATE)
        val jrCacheFilePathColumnIndex =
            cursor.getColumnIndex(DatabaseFormColumns.JRCACHE_FILE_PATH)
        val formMediaPathColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.FORM_MEDIA_PATH)
        val languageColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.LANGUAGE)
        val autoSendColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.AUTO_SEND)
        val autoDeleteColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.AUTO_DELETE)
        val geometryXpathColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.GEOMETRY_XPATH)
        val deletedDateColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DELETED_DATE)
        return Form.Builder()
            .dbId(cursor.getLong(idColumnIndex))
            .displayName(cursor.getString(displayNameColumnIndex))
            .description(cursor.getString(descriptionColumnIndex))
            .formId(cursor.getString(jrFormIdColumnIndex))
            .version(cursor.getString(jrVersionColumnIndex))
            .formFilePath(
                getAbsoluteFilePath(
                    formsPath,
                    cursor.getString(formFilePathColumnIndex)
                )
            )
            .submissionUri(cursor.getString(submissionUriColumnIndex))
            .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
            .md5Hash(cursor.getString(md5HashColumnIndex))
            .date(cursor.getLong(dateColumnIndex))
            .jrCacheFilePath(
                getAbsoluteFilePath(
                    cachePath,
                    cursor.getString(jrCacheFilePathColumnIndex)
                )
            )
            .formMediaPath(
                getAbsoluteFilePath(
                    formsPath,
                    cursor.getString(formMediaPathColumnIndex)
                )
            )
            .language(cursor.getString(languageColumnIndex))
            .autoSend(cursor.getString(autoSendColumnIndex))
            .autoDelete(cursor.getString(autoDeleteColumnIndex))
            .geometryXpath(cursor.getString(geometryXpathColumnIndex))
            .deleted(!cursor.isNull(deletedDateColumnIndex))
            .build()
    }

    @JvmStatic
    fun getInstanceFromValues(values: ContentValues): Instance? {
        return Instance.Builder()
            .dbId(values.getAsLong(BaseColumns._ID))
            .displayName(values.getAsString(DatabaseInstanceColumns.DISPLAY_NAME))
            .submissionUri(values.getAsString(DatabaseInstanceColumns.SUBMISSION_URI))
            .canEditWhenComplete(Boolean.parseBoolean(values.getAsString(DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE)))
            .instanceFilePath(values.getAsString(DatabaseInstanceColumns.INSTANCE_FILE_PATH))
            .formId(values.getAsString(DatabaseInstanceColumns.JR_FORM_ID))
            .formVersion(values.getAsString(DatabaseInstanceColumns.JR_VERSION))
            .status(values.getAsString(DatabaseInstanceColumns.STATUS))
            .lastStatusChangeDate(values.getAsLong(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE))
            .deletedDate(values.getAsLong(DatabaseInstanceColumns.DELETED_DATE))
            .geometry(values.getAsString(DatabaseInstanceColumns.GEOMETRY))
            .geometryType(values.getAsString(DatabaseInstanceColumns.GEOMETRY_TYPE))
            .build()
    }

    @JvmStatic
    fun getInstanceFromCurrentCursorPosition(cursor: Cursor, instancesPath: String): Instance? {
        val dbId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
        val displayNameColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.DISPLAY_NAME)
        val submissionUriColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.SUBMISSION_URI)
        val canEditWhenCompleteIndex =
            cursor.getColumnIndex(DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE)
        val instanceFilePathIndex =
            cursor.getColumnIndex(DatabaseInstanceColumns.INSTANCE_FILE_PATH)
        val jrFormIdColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.JR_FORM_ID)
        val jrVersionColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.JR_VERSION)
        val statusColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.STATUS)
        val lastStatusChangeDateColumnIndex =
            cursor.getColumnIndex(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE)
        val deletedDateColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.DELETED_DATE)
        val geometryTypeColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.GEOMETRY_TYPE)
        val geometryColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.GEOMETRY)
        val databaseIdIndex = cursor.getColumnIndex(BaseColumns._ID)
        return Instance.Builder()
            .dbId(dbId)
            .displayName(cursor.getString(displayNameColumnIndex))
            .submissionUri(cursor.getString(submissionUriColumnIndex))
            .canEditWhenComplete(Boolean.valueOf(cursor.getString(canEditWhenCompleteIndex)))
            .instanceFilePath(
                getAbsoluteFilePath(
                    instancesPath,
                    cursor.getString(instanceFilePathIndex)
                )
            )
            .formId(cursor.getString(jrFormIdColumnIndex))
            .formVersion(cursor.getString(jrVersionColumnIndex))
            .status(cursor.getString(statusColumnIndex))
            .lastStatusChangeDate(cursor.getLong(lastStatusChangeDateColumnIndex))
            .deletedDate(
                if (cursor.isNull(deletedDateColumnIndex)) null else cursor.getLong(
                    deletedDateColumnIndex
                )
            )
            .geometryType(cursor.getString(geometryTypeColumnIndex))
            .geometry(cursor.getString(geometryColumnIndex))
            .dbId(cursor.getLong(databaseIdIndex))
            .build()
    }

    @JvmStatic
    fun getValuesFromInstance(instance: Instance, instancesPath: String): ContentValues {
        val values = ContentValues()
        values.put(BaseColumns._ID, instance.dbId)
        values.put(DatabaseInstanceColumns.DISPLAY_NAME, instance.displayName)
        values.put(DatabaseInstanceColumns.SUBMISSION_URI, instance.submissionUri)
        values.put(
            DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE,
            Boolean.toString(instance.canEditWhenComplete())
        )
        values.put(
            DatabaseInstanceColumns.INSTANCE_FILE_PATH,
            getRelativeFilePath(instancesPath, instance.instanceFilePath)
        )
        values.put(DatabaseInstanceColumns.JR_FORM_ID, instance.formId)
        values.put(DatabaseInstanceColumns.JR_VERSION, instance.formVersion)
        values.put(DatabaseInstanceColumns.STATUS, instance.status)
        values.put(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE, instance.lastStatusChangeDate)
        values.put(DatabaseInstanceColumns.DELETED_DATE, instance.deletedDate)
        values.put(DatabaseInstanceColumns.GEOMETRY, instance.geometry)
        values.put(DatabaseInstanceColumns.GEOMETRY_TYPE, instance.geometryType)
        return values
    }
}
