package org.odk.collect.android.database

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.forms.Form

/**
 * Columns for the Forms table.
 */
object DatabaseFormColumns : BaseColumns {
    // These are the only things needed for an insert
    const val DISPLAY_NAME = "displayName"
    const val DESCRIPTION = "description" // can be null
    const val JR_FORM_ID = "jrFormId"
    const val JR_VERSION = "jrVersion" // can be null
    const val FORM_FILE_PATH = "formFilePath"
    const val SUBMISSION_URI = "submissionUri" // can be null
    const val BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey" // can be null
    const val AUTO_DELETE = "autoDelete" // can be null

    // Column is called autoSubmit for legacy support but the attribute is auto-send
    const val AUTO_SEND = "autoSubmit" // can be null
    const val GEOMETRY_XPATH = "geometryXpath" // can be null

    // these are generated for you (but you can insert something else if you want)
    const val DISPLAY_SUBTEXT = "displaySubtext" // not used in the newest database version
    const val MD5_HASH = "md5Hash"
    const val DATE = "date"
    const val MAX_DATE = "MAX(date)" // used only to get latest forms for each form_id
    const val JRCACHE_FILE_PATH = "jrcacheFilePath"
    const val FORM_MEDIA_PATH = "formMediaPath"

    // this is null on create, and can only be set on an update.
    const val LANGUAGE = "language"
    const val DELETED_DATE = "deleted_date"

    @JvmStatic
    fun getValuesFromForm(form: Form, storagePathProvider: StoragePathProvider): ContentValues? {
        val values = ContentValues()
        values.put(BaseColumns._ID, form.dbId)
        values.put(DISPLAY_NAME, form.displayName)
        values.put(DESCRIPTION, form.description)
        values.put(JR_FORM_ID, form.formId)
        values.put(JR_VERSION, form.version)
        values.put(FORM_FILE_PATH, storagePathProvider.getRelativeFormPath(form.formFilePath))
        values.put(SUBMISSION_URI, form.submissionUri)
        values.put(BASE64_RSA_PUBLIC_KEY, form.basE64RSAPublicKey)
        values.put(MD5_HASH, form.mD5Hash)
        values.put(FORM_MEDIA_PATH, storagePathProvider.getRelativeFormPath(form.formMediaPath))
        values.put(LANGUAGE, form.language)
        values.put(AUTO_SEND, form.autoSend)
        values.put(AUTO_DELETE, form.autoDelete)
        values.put(GEOMETRY_XPATH, form.geometryXpath)
        return values
    }

    @JvmStatic
    fun getFormFromValues(values: ContentValues, storagePathProvider: StoragePathProvider): Form? {
        return Form.Builder()
            .dbId(values.getAsLong(BaseColumns._ID))
            .displayName(values.getAsString(DISPLAY_NAME))
            .description(values.getAsString(DESCRIPTION))
            .formId(values.getAsString(JR_FORM_ID))
            .version(values.getAsString(JR_VERSION))
            .formFilePath(storagePathProvider.getAbsoluteFormFilePath(values.getAsString(FORM_FILE_PATH)))
            .submissionUri(values.getAsString(SUBMISSION_URI))
            .base64RSAPublicKey(values.getAsString(BASE64_RSA_PUBLIC_KEY))
            .md5Hash(values.getAsString(MD5_HASH))
            .date(values.getAsLong(DATE))
            .jrCacheFilePath(storagePathProvider.getAbsoluteCacheFilePath(values.getAsString(JRCACHE_FILE_PATH)))
            .formMediaPath(storagePathProvider.getAbsoluteFormFilePath(values.getAsString(FORM_MEDIA_PATH)))
            .language(values.getAsString(LANGUAGE))
            .autoSend(values.getAsString(AUTO_SEND))
            .autoDelete(values.getAsString(AUTO_DELETE))
            .geometryXpath(values.getAsString(GEOMETRY_XPATH))
            .deleted(values.getAsLong(DELETED_DATE) != null)
            .build()
    }

    @JvmStatic
    fun getFormFromCurrentCursorPosition(cursor: Cursor, storagePathProvider: StoragePathProvider): Form? {
        val idColumnIndex = cursor.getColumnIndex(BaseColumns._ID)
        val displayNameColumnIndex = cursor.getColumnIndex(DISPLAY_NAME)
        val descriptionColumnIndex = cursor.getColumnIndex(DESCRIPTION)
        val jrFormIdColumnIndex = cursor.getColumnIndex(JR_FORM_ID)
        val jrVersionColumnIndex = cursor.getColumnIndex(JR_VERSION)
        val formFilePathColumnIndex = cursor.getColumnIndex(FORM_FILE_PATH)
        val submissionUriColumnIndex = cursor.getColumnIndex(SUBMISSION_URI)
        val base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(BASE64_RSA_PUBLIC_KEY)
        val md5HashColumnIndex = cursor.getColumnIndex(MD5_HASH)
        val dateColumnIndex = cursor.getColumnIndex(DATE)
        val jrCacheFilePathColumnIndex = cursor.getColumnIndex(JRCACHE_FILE_PATH)
        val formMediaPathColumnIndex = cursor.getColumnIndex(FORM_MEDIA_PATH)
        val languageColumnIndex = cursor.getColumnIndex(LANGUAGE)
        val autoSendColumnIndex = cursor.getColumnIndex(AUTO_SEND)
        val autoDeleteColumnIndex = cursor.getColumnIndex(AUTO_DELETE)
        val geometryXpathColumnIndex = cursor.getColumnIndex(GEOMETRY_XPATH)
        val deletedDateColumnIndex = cursor.getColumnIndex(DELETED_DATE)
        return Form.Builder()
            .dbId(cursor.getLong(idColumnIndex))
            .displayName(cursor.getString(displayNameColumnIndex))
            .description(cursor.getString(descriptionColumnIndex))
            .formId(cursor.getString(jrFormIdColumnIndex))
            .version(cursor.getString(jrVersionColumnIndex))
            .formFilePath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formFilePathColumnIndex)))
            .submissionUri(cursor.getString(submissionUriColumnIndex))
            .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
            .md5Hash(cursor.getString(md5HashColumnIndex))
            .date(cursor.getLong(dateColumnIndex))
            .jrCacheFilePath(storagePathProvider.getAbsoluteCacheFilePath(cursor.getString(jrCacheFilePathColumnIndex)))
            .formMediaPath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formMediaPathColumnIndex)))
            .language(cursor.getString(languageColumnIndex))
            .autoSend(cursor.getString(autoSendColumnIndex))
            .autoDelete(cursor.getString(autoDeleteColumnIndex))
            .geometryXpath(cursor.getString(geometryXpathColumnIndex))
            .deleted(!cursor.isNull(deletedDateColumnIndex))
            .build()
    }
}
