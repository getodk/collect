package org.odk.collect.android.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.odk.collect.android.utilities.SQLiteUtils;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DELETED_DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DESCRIPTION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LANGUAGE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.PROJECT;       // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.TASKS_ONLY;    // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SOURCE;        // smap

public class FormDatabaseMigrator {

    private static final String[] COLUMN_NAMES_V7 = {_ID, DISPLAY_NAME, DESCRIPTION,
            JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
            SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
            "lastDetectedFormVersionHash"};

    // These exist in database versions 2 and 3, but not in 4...
    private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
    private static final String MODEL_VERSION = "modelVersion";

    public void onCreate(SQLiteDatabase db) {
        createFormsTableV17(db);    // smap
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

            if(oldVersion < 17) {   // smap - start from 17
                upgradeToVersion17(db);
            }

            Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }


    // smap sarting point for upgrades
    private void upgradeToVersion17(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_SEND, "text");     // Version 5
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_DELETE, "text");   // Version 7
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, LAST_DETECTED_FORM_VERSION_HASH, "text");    // smap retain in case of downgrade
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, PROJECT, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, TASKS_ONLY, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, SOURCE, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, GEOMETRY_XPATH, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, deleted, "boolean default(0)");

    }
    // smap
    private static void createFormsTableV17(SQLiteDatabase db) {
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
                + LAST_DETECTED_FORM_VERSION_HASH + " text,"  // smap keep for downgrading
                + GEOMETRY_XPATH + " text,"
                + PROJECT + " text,"
                + TASKS_ONLY + " text,"
                + SOURCE + " text,"
                + "displaySubtext text ",   // Smap keep for downgrading
                + "deleted" + " boolean default(0));");
    }

    public static void databaseMigrationStarted() {
        isDatabaseBeingMigrated = true;
    }

    // smap
    public static void recreateDatabase() {

        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(FormsDatabaseHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READWRITE);
            SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
            createFormsTableV17(db);
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
    }
}
