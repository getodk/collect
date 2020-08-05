package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
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
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns._ID;

@RunWith(AndroidJUnit4.class)
public class FormDatabaseMigratorTest {

    @Test
    public void onUpgrade_fromVersion8_toVersion9() {
        assertThat("Test expects different Forms DB version", DatabaseConstants.FORMS_DATABASE_VERSION, is(9));

        SQLiteDatabase database = SQLiteDatabase.create(null);
        createVersion8Database(database);

        ContentValues contentValues = createVersion8Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        new FormDatabaseMigrator().onUpgrade(database, 8, 9);


        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(18));
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
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_DETECTED_FORM_VERSION_HASH)), is(contentValues.getAsString(LAST_DETECTED_FORM_VERSION_HASH)));
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY_XPATH)), is(contentValues.getAsString(GEOMETRY_XPATH)));
            assertThat(cursor.getInt(cursor.getColumnIndex(DESCRIPTION)), is(0));
        }
    }

    @NotNull
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
        contentValues.put(LAST_DETECTED_FORM_VERSION_HASH, "LastDetectedFormVersionHash");
        contentValues.put(GEOMETRY_XPATH, "GeometryXPath");
        return contentValues;
    }

    private void createVersion8Database(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
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
                + LAST_DETECTED_FORM_VERSION_HASH + " text, "
                + GEOMETRY_XPATH + " text);");
    }
}