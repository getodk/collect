package org.odk.collect.android.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.odk.collect.android.utilities.SQLiteUtils;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.DatabaseConstants.FORMS_DATABASE_VERSION;
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
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.PROJECT;       // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.TASKS_ONLY;    // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.READ_ONLY;    // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SEARCH_LOCAL_DATA;    // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SOURCE;        // smap

public class FormDatabaseMigrator implements DatabaseMigrator {


    public void onCreate(SQLiteDatabase db) {
        createLatestVersion(db);    // smap
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    public void onUpgrade(SQLiteDatabase db, int oldVersion) {
        try {

            if(oldVersion < FORMS_DATABASE_VERSION) {   // smap
                upgradeToLatestVersion(db);
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    // smap starting point for upgrades
    private void upgradeToLatestVersion(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_SEND, "text");     // Version 5
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_DELETE, "text");   // Version 7

        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, LAST_DETECTED_FORM_VERSION_HASH, "text");

        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, PROJECT, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, TASKS_ONLY, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, READ_ONLY, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, SOURCE, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, DELETED_DATE, "integer");

        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, GEOMETRY_XPATH, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, SEARCH_LOCAL_DATA, "text");

    }

    public void onDowngrade(SQLiteDatabase db) throws SQLException {
        // smap - no action
    }

    // smap
    private static void createLatestVersion(SQLiteDatabase db) {
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
                + LAST_DETECTED_FORM_VERSION_HASH + " text,"
                + GEOMETRY_XPATH + " text,"
                + PROJECT + " text,"    // smap
                + TASKS_ONLY + " text," // smap
                + READ_ONLY + " text," // smap
                + SEARCH_LOCAL_DATA + " text," // smap
                + SOURCE + " text,"     // smap
                + DELETED_DATE + " integer,"

                + "displaySubtext text, "         // Smap keep for downgrading
                + "deleted boolean default(0) "   // Smap keep for downgrading
                +");");
    }

    // smap
    public static void recreateDatabase() {

        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(FormsDatabaseHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READWRITE);
            SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
            createLatestVersion(db);
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
    }
}

