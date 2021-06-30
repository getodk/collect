package org.odk.collect.android.database.forms

import android.provider.BaseColumns

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
    const val JRCACHE_FILE_PATH = "jrcacheFilePath"
    const val FORM_MEDIA_PATH = "formMediaPath"

    // this is null on create, and can only be set on an update.
    const val LANGUAGE = "language"
    const val DELETED_DATE = "deleted_date"
}
