package org.odk.collect.android.database.forms;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.odk.collect.db.sqlite.DatabaseMigrator;
import org.odk.collect.db.sqlite.SQLiteUtils;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_DELETE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_SEND;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DELETED_DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DESCRIPTION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DISPLAY_SUBTEXT;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.USES_ENTITIES;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_FORM_ID;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_VERSION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.LANGUAGE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.LAST_DETECTED_ATTACHMENTS_UPDATE_DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.MD5_HASH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.SUBMISSION_URI;

import timber.log.Timber;

public class FormDatabaseMigrator implements DatabaseMigrator {

    private static final String[] COLUMN_NAMES_V7 = {_ID, DISPLAY_NAME, DESCRIPTION,
            JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
            SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
            LAST_DETECTED_FORM_VERSION_HASH};

    // These exist in database versions 2 and 3, but not in 4...
    private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
    private static final String MODEL_VERSION = "modelVersion";

    public void onCreate(SQLiteDatabase db) {
        createFormsTableV14(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    public void onUpgrade(SQLiteDatabase db, int oldVersion) throws SQLException {
        Timber.w("Forms db upgrade from version: %s", oldVersion);
        switch (oldVersion) {
            case 1:
                upgradeToVersion2(db);
            case 2:
            case 3:
                upgradeToVersion4(db, oldVersion);
            case 4:
                upgradeToVersion5(db);
            case 5:
                upgradeToVersion6(db);
            case 6:
                upgradeToVersion7(db);
            case 7:
                upgradeToVersion8(db);
            case 8:
                upgradeToVersion9(db);
            case 9:
                upgradeToVersion10(db);
            case 10:
                upgradeToVersion11(db);
            case 11:
                upgradeToVersion12(db);
            case 12:
                upgradeToVersion13(db);
            case 13:
                upgradeToVersion14(db);
            case 14:
                // Remember to bump the database version number in {@link org.odk.collect.android.database.DatabaseConstants}
                // upgradeToVersion15(db);
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
        onCreate(db);
    }

    private void upgradeToVersion4(SQLiteDatabase db, int oldVersion) {
        // adding BASE64_RSA_PUBLIC_KEY and changing type and name of
        // integer MODEL_VERSION to text VERSION
        SQLiteUtils.dropTable(db, TEMP_FORMS_TABLE_NAME);
        createFormsTableV4(db, TEMP_FORMS_TABLE_NAME);
        db.execSQL("INSERT INTO "
                + TEMP_FORMS_TABLE_NAME
                + " ("
                + _ID
                + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH
                + ", "
                + FORM_FILE_PATH
                + ", "
                + LANGUAGE
                + ", "
                + SUBMISSION_URI
                + ", "
                + JR_VERSION
                + ", "
                + ((oldVersion != 3) ? ""
                : (BASE64_RSA_PUBLIC_KEY + ", "))
                + JRCACHE_FILE_PATH
                + ") SELECT "
                + _ID
                + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH
                + ", "
                + FORM_FILE_PATH
                + ", "
                + LANGUAGE
                + ", "
                + SUBMISSION_URI
                + ", "
                + "CASE WHEN "
                + MODEL_VERSION
                + " IS NOT NULL THEN "
                + "CAST("
                + MODEL_VERSION
                + " AS TEXT) ELSE NULL END, "
                + ((oldVersion != 3) ? ""
                : (BASE64_RSA_PUBLIC_KEY + ", "))
                + JRCACHE_FILE_PATH + " FROM "
                + FORMS_TABLE_NAME);

        // risky failures here...
        SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
        createFormsTableV4(db, FORMS_TABLE_NAME);
        db.execSQL("INSERT INTO "
                + FORMS_TABLE_NAME
                + " ("
                + _ID
                + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH + ", "
                + FORM_FILE_PATH + ", "
                + LANGUAGE + ", "
                + SUBMISSION_URI + ", "
                + JR_VERSION + ", "
                + BASE64_RSA_PUBLIC_KEY + ", "
                + JRCACHE_FILE_PATH + ") SELECT "
                + _ID + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH + ", "
                + FORM_FILE_PATH + ", "
                + LANGUAGE + ", "
                + SUBMISSION_URI + ", "
                + JR_VERSION + ", "
                + BASE64_RSA_PUBLIC_KEY + ", "
                + JRCACHE_FILE_PATH + " FROM "
                + TEMP_FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, TEMP_FORMS_TABLE_NAME);
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_SEND, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_DELETE, "text");
    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, LAST_DETECTED_FORM_VERSION_HASH, "text");
    }

    private void upgradeToVersion7(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV7(db);
        SQLiteUtils.copyRows(db, temporaryTable, COLUMN_NAMES_V7, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion8(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, GEOMETRY_XPATH, "text");
    }

    private void upgradeToVersion9(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV9(db);
        SQLiteUtils.copyRows(db, temporaryTable, new String[]{_ID, DISPLAY_NAME, DESCRIPTION,
                JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
                SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
                GEOMETRY_XPATH}, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion10(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV10(db);
        SQLiteUtils.copyRows(db, temporaryTable, new String[]{_ID, DISPLAY_NAME, DESCRIPTION,
                JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
                SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
                GEOMETRY_XPATH}, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion11(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV11(db);
        SQLiteUtils.copyRows(db, temporaryTable, new String[]{_ID, DISPLAY_NAME, DESCRIPTION,
                JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
                SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
                GEOMETRY_XPATH, DELETED_DATE}, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion12(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, LAST_DETECTED_ATTACHMENTS_UPDATE_DATE, "integer");
    }

    private void upgradeToVersion13(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV13(db);
        SQLiteUtils.copyRows(db, temporaryTable, new String[]{_ID, DISPLAY_NAME, DESCRIPTION,
                JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
                SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
                GEOMETRY_XPATH, DELETED_DATE, LAST_DETECTED_ATTACHMENTS_UPDATE_DATE}, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion14(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, USES_ENTITIES, "text");
    }

    private void createFormsTableV4(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DISPLAY_SUBTEXT + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + LAST_DETECTED_FORM_VERSION_HASH + " text);");
    }

    public void createFormsTableV7(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + LAST_DETECTED_FORM_VERSION_HASH + " text);");
    }

    public void createFormsTableV8(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, "
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + LAST_DETECTED_FORM_VERSION_HASH + " text, "
                + GEOMETRY_XPATH + " text);");
    }

    public void createFormsTableV9(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + GEOMETRY_XPATH + " text, "
                + "deleted" + " boolean default(0));");
    }

    public void createFormsTableV10(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + GEOMETRY_XPATH + " text, "
                + DELETED_DATE + " integer);");
    }

    public void createFormsTableV11(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null UNIQUE ON CONFLICT IGNORE, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + GEOMETRY_XPATH + " text, "
                + DELETED_DATE + " integer);");
    }

    public void createFormsTableV12(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null UNIQUE ON CONFLICT IGNORE, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + GEOMETRY_XPATH + " text, "
                + DELETED_DATE + " integer, "
                + LAST_DETECTED_ATTACHMENTS_UPDATE_DATE + " integer);"); // milliseconds
    }

    public void createFormsTableV13(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null UNIQUE ON CONFLICT IGNORE, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + GEOMETRY_XPATH + " text, "
                + DELETED_DATE + " integer, "
                + LAST_DETECTED_ATTACHMENTS_UPDATE_DATE + " integer);"); // milliseconds
    }

    private void createFormsTableV14(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null UNIQUE ON CONFLICT IGNORE, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + GEOMETRY_XPATH + " text, "
                + DELETED_DATE + " integer, "
                + LAST_DETECTED_ATTACHMENTS_UPDATE_DATE + " integer, " // milliseconds
                + USES_ENTITIES + " text);");
    }
}
