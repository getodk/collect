package org.odk.collect.android.database;

import android.provider.BaseColumns;

/**
 * Columns for the Forms table.
 */
public final class DatabaseFormColumns implements BaseColumns {

    // These are the only things needed for an insert
    public static final String DISPLAY_NAME = "displayName";
    public static final String DESCRIPTION = "description";  // can be null
    public static final String JR_FORM_ID = "jrFormId";
    public static final String JR_VERSION = "jrVersion"; // can be null
    public static final String FORM_FILE_PATH = "formFilePath";
    public static final String SUBMISSION_URI = "submissionUri"; // can be null
    public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey"; // can be null
    public static final String AUTO_DELETE = "autoDelete"; // can be null
    // Column is called autoSubmit for legacy support but the attribute is auto-send
    public static final String AUTO_SEND = "autoSubmit"; // can be null
    public static final String GEOMETRY_XPATH = "geometryXpath"; // can be null

    // these are generated for you (but you can insert something else if you want)
    public static final String DISPLAY_SUBTEXT = "displaySubtext"; // not used in the newest database version
    public static final String MD5_HASH = "md5Hash";
    public static final String DATE = "date";
    public static final String MAX_DATE = "MAX(date)"; // used only to get latest forms for each form_id
    public static final String JRCACHE_FILE_PATH = "jrcacheFilePath";
    public static final String FORM_MEDIA_PATH = "formMediaPath";

    // this is null on create, and can only be set on an update.
    public static final String LANGUAGE = "language";

    public static final String DELETED_DATE = "deleted_date";

    private DatabaseFormColumns() {
    }
}
