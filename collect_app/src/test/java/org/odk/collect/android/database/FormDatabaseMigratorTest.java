package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.database.forms.FormDatabaseMigrator;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_DELETE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_SEND;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DELETED_DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DESCRIPTION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_FORM_ID;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_VERSION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.LANGUAGE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.MD5_HASH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.SUBMISSION_URI;
import static org.odk.collect.android.database.forms.DatabaseFormColumns._ID;

@RunWith(AndroidJUnit4.class)
public class FormDatabaseMigratorTest {

    public static final List<String> CURRENT_VERSION_COLUMNS = asList(_ID, DISPLAY_NAME, DESCRIPTION,
            JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
            SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
            GEOMETRY_XPATH, DELETED_DATE);

    private SQLiteDatabase database;

    @Before
    public void setup() {
        assertThat("Test expects different Forms DB version", DatabaseConstants.FORMS_DATABASE_VERSION, is(10));
        database = SQLiteDatabase.create(null);
    }

    @After
    public void teardown() {
        database.close();
    }

    @Test
    public void onUpgrade_fromVersion9() {
        createVersion9Database(database);
        ContentValues contentValues = createVersion9Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        new FormDatabaseMigrator().onUpgrade(database, 9);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(17));
            assertThat(cursor.getCount(), is(1));

            cursor.moveToFirst();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is(contentValues.getAsString(DISPLAY_NAME)));
            assertThat(cursor.getString(cursor.getColumnIndex(DESCRIPTION)), is(contentValues.getAsString(DESCRIPTION)));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(contentValues.getAsString(JR_FORM_ID)));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is(contentValues.getAsString(JR_VERSION)));
            assertThat(cursor.getString(cursor.getColumnIndex(MD5_HASH)), is(contentValues.getAsString(MD5_HASH)));
            assertThat(cursor.getInt(cursor.getColumnIndex(DATE)), is(contentValues.getAsInteger(DATE)));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH)), is(contentValues.getAsString(FORM_MEDIA_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH)), is(contentValues.getAsString(FORM_FILE_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(LANGUAGE)), is(contentValues.getAsString(LANGUAGE)));
            assertThat(cursor.getString(cursor.getColumnIndex(SUBMISSION_URI)), is(contentValues.getAsString(SUBMISSION_URI)));
            assertThat(cursor.getString(cursor.getColumnIndex(BASE64_RSA_PUBLIC_KEY)), is(contentValues.getAsString(BASE64_RSA_PUBLIC_KEY)));
            assertThat(cursor.getString(cursor.getColumnIndex(JRCACHE_FILE_PATH)), is(contentValues.getAsString(JRCACHE_FILE_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(AUTO_SEND)), is(contentValues.getAsString(AUTO_SEND)));
            assertThat(cursor.getString(cursor.getColumnIndex(AUTO_DELETE)), is(contentValues.getAsString(AUTO_DELETE)));
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY_XPATH)), is(contentValues.getAsString(GEOMETRY_XPATH)));
            assertThat(cursor.isNull(cursor.getColumnIndex(DELETED_DATE)), is(true));
        }
    }

    @Test
    public void onUpgrade_fromVersion8() {
        createVersion8Database(database);
        ContentValues contentValues = createVersion8Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        new FormDatabaseMigrator().onUpgrade(database, 8);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(17));
            assertThat(cursor.getCount(), is(1));

            cursor.moveToFirst();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is(contentValues.getAsString(DISPLAY_NAME)));
            assertThat(cursor.getString(cursor.getColumnIndex(DESCRIPTION)), is(contentValues.getAsString(DESCRIPTION)));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(contentValues.getAsString(JR_FORM_ID)));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is(contentValues.getAsString(JR_VERSION)));
            assertThat(cursor.getString(cursor.getColumnIndex(MD5_HASH)), is(contentValues.getAsString(MD5_HASH)));
            assertThat(cursor.getInt(cursor.getColumnIndex(DATE)), is(contentValues.getAsInteger(DATE)));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH)), is(contentValues.getAsString(FORM_MEDIA_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH)), is(contentValues.getAsString(FORM_FILE_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(LANGUAGE)), is(contentValues.getAsString(LANGUAGE)));
            assertThat(cursor.getString(cursor.getColumnIndex(SUBMISSION_URI)), is(contentValues.getAsString(SUBMISSION_URI)));
            assertThat(cursor.getString(cursor.getColumnIndex(BASE64_RSA_PUBLIC_KEY)), is(contentValues.getAsString(BASE64_RSA_PUBLIC_KEY)));
            assertThat(cursor.getString(cursor.getColumnIndex(JRCACHE_FILE_PATH)), is(contentValues.getAsString(JRCACHE_FILE_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(AUTO_SEND)), is(contentValues.getAsString(AUTO_SEND)));
            assertThat(cursor.getString(cursor.getColumnIndex(AUTO_DELETE)), is(contentValues.getAsString(AUTO_DELETE)));
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY_XPATH)), is(contentValues.getAsString(GEOMETRY_XPATH)));
            assertThat(cursor.isNull(cursor.getColumnIndex(DELETED_DATE)), is(true));
        }
    }

    @Test
    public void onUpgrade_fromVersion7() {
        createVersion7Database(database);
        ContentValues contentValues = createVersion7Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        new FormDatabaseMigrator().onUpgrade(database, 7);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(17));
            assertThat(cursor.getCount(), is(1));

            cursor.moveToFirst();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is(contentValues.getAsString(DISPLAY_NAME)));
            assertThat(cursor.getString(cursor.getColumnIndex(DESCRIPTION)), is(contentValues.getAsString(DESCRIPTION)));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(contentValues.getAsString(JR_FORM_ID)));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is(contentValues.getAsString(JR_VERSION)));
            assertThat(cursor.getString(cursor.getColumnIndex(MD5_HASH)), is(contentValues.getAsString(MD5_HASH)));
            assertThat(cursor.getInt(cursor.getColumnIndex(DATE)), is(contentValues.getAsInteger(DATE)));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH)), is(contentValues.getAsString(FORM_MEDIA_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH)), is(contentValues.getAsString(FORM_FILE_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(LANGUAGE)), is(contentValues.getAsString(LANGUAGE)));
            assertThat(cursor.getString(cursor.getColumnIndex(SUBMISSION_URI)), is(contentValues.getAsString(SUBMISSION_URI)));
            assertThat(cursor.getString(cursor.getColumnIndex(BASE64_RSA_PUBLIC_KEY)), is(contentValues.getAsString(BASE64_RSA_PUBLIC_KEY)));
            assertThat(cursor.getString(cursor.getColumnIndex(JRCACHE_FILE_PATH)), is(contentValues.getAsString(JRCACHE_FILE_PATH)));
            assertThat(cursor.getString(cursor.getColumnIndex(AUTO_SEND)), is(contentValues.getAsString(AUTO_SEND)));
            assertThat(cursor.getString(cursor.getColumnIndex(AUTO_DELETE)), is(contentValues.getAsString(AUTO_DELETE)));
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY_XPATH)), is(contentValues.getAsString(GEOMETRY_XPATH)));
            assertThat(cursor.isNull(cursor.getColumnIndex(DELETED_DATE)), is(true));
        }
    }

    @Test
    public void onDowngrade_fromVersionWithExtraColumn() {
        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
        formDatabaseMigrator.onCreate(database);
        SQLiteUtils.addColumn(database, FORMS_TABLE_NAME, "new_column", "text");
        ContentValues contentValues = createVersion8Form();
        contentValues.put("new_column", "blah");
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onDowngrade(database);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(17));
            assertThat(cursor.getCount(), is(0));
            assertThat(asList(cursor.getColumnNames()), is(CURRENT_VERSION_COLUMNS));
        }
    }

    @Test
    public void onDowngrade_fromVersionWithMissingColumn() {
        // Create form table with out JR Cache column
        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
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
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + "lastDetectedFormVersionHash" + " text, "
                + GEOMETRY_XPATH + " text);");

        ContentValues contentValues = createVersion8Form();
        contentValues.remove(JRCACHE_FILE_PATH);
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onDowngrade(database);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(17));
            assertThat(cursor.getCount(), is(0));
            assertThat(asList(cursor.getColumnNames()), is(CURRENT_VERSION_COLUMNS));
        }
    }

    private ContentValues createVersion8Form() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DISPLAY_NAME, "DisplayName");
        contentValues.put(DESCRIPTION, "Description");
        contentValues.put(JR_FORM_ID, "FormId");
        contentValues.put(JR_VERSION, "FormVersion");
        contentValues.put(MD5_HASH, "Md5Hash");
        contentValues.put(DATE, 0);
        contentValues.put(FORM_MEDIA_PATH, "Form/Media/Path");
        contentValues.put(FORM_FILE_PATH, "Form/File/Path");
        contentValues.put(LANGUAGE, "Language");
        contentValues.put(SUBMISSION_URI, "submission.uri");
        contentValues.put(BASE64_RSA_PUBLIC_KEY, "Base64RsaPublicKey");
        contentValues.put(JRCACHE_FILE_PATH, "Jr/Cache/File/Path");
        contentValues.put(AUTO_SEND, "AutoSend");
        contentValues.put(AUTO_DELETE, "AutoDelete");
        contentValues.put("lastDetectedFormVersionHash", "LastDetectedFormVersionHash");
        contentValues.put(GEOMETRY_XPATH, "GeometryXPath");
        return contentValues;
    }

    private ContentValues createVersion7Form() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DISPLAY_NAME, "DisplayName");
        contentValues.put(DESCRIPTION, "Description");
        contentValues.put(JR_FORM_ID, "FormId");
        contentValues.put(JR_VERSION, "FormVersion");
        contentValues.put(MD5_HASH, "Md5Hash");
        contentValues.put(DATE, 0);
        contentValues.put(FORM_MEDIA_PATH, "Form/Media/Path");
        contentValues.put(FORM_FILE_PATH, "Form/File/Path");
        contentValues.put(LANGUAGE, "Language");
        contentValues.put(SUBMISSION_URI, "submission.uri");
        contentValues.put(BASE64_RSA_PUBLIC_KEY, "Base64RsaPublicKey");
        contentValues.put(JRCACHE_FILE_PATH, "Jr/Cache/File/Path");
        contentValues.put(AUTO_SEND, "AutoSend");
        contentValues.put(AUTO_DELETE, "AutoDelete");
        contentValues.put("lastDetectedFormVersionHash", "LastDetectedFormVersionHash");
        return contentValues;
    }

    private ContentValues createVersion9Form() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DISPLAY_NAME, "DisplayName");
        contentValues.put(DESCRIPTION, "Description");
        contentValues.put(JR_FORM_ID, "FormId");
        contentValues.put(JR_VERSION, "FormVersion");
        contentValues.put(MD5_HASH, "Md5Hash");
        contentValues.put(DATE, 0);
        contentValues.put(FORM_MEDIA_PATH, "Form/Media/Path");
        contentValues.put(FORM_FILE_PATH, "Form/File/Path");
        contentValues.put(LANGUAGE, "Language");
        contentValues.put(SUBMISSION_URI, "submission.uri");
        contentValues.put(BASE64_RSA_PUBLIC_KEY, "Base64RsaPublicKey");
        contentValues.put(JRCACHE_FILE_PATH, "Jr/Cache/File/Path");
        contentValues.put(AUTO_SEND, "AutoSend");
        contentValues.put(AUTO_DELETE, "AutoDelete");
        contentValues.put(GEOMETRY_XPATH, "GeometryXPath");
        contentValues.put("deleted", 0);
        return contentValues;
    }

    private void createVersion7Database(SQLiteDatabase database) {
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
                + "lastDetectedFormVersionHash" + " text);");
    }

    private void createVersion8Database(SQLiteDatabase database) {
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
                + "lastDetectedFormVersionHash" + " text, "
                + GEOMETRY_XPATH + " text);");
    }

    private void createVersion9Database(SQLiteDatabase db) {
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
}
