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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_DELETE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_SEND;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DELETED_DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DESCRIPTION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DISPLAY_NAME;
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
import static org.odk.collect.android.database.forms.DatabaseFormColumns._ID;

@RunWith(AndroidJUnit4.class)
public class FormDatabaseMigratorTest {

    private SQLiteDatabase database;

    @Before
    public void setup() {
        assertThat("Test expects different Forms DB version", DatabaseConstants.FORMS_DATABASE_VERSION, is(14));
        database = SQLiteDatabase.create(null);
    }

    @After
    public void teardown() {
        database.close();
    }

    @Test
    public void databaseIdsShouldNotBeReused() {
        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
        formDatabaseMigrator.onCreate(database);

        ContentValues contentValues = getContentValuesForFormV13();
        database.insert(FORMS_TABLE_NAME, null, contentValues);
        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getCount(), is(1));
            cursor.moveToFirst();
            assertThat(cursor.getInt(cursor.getColumnIndex(_ID)), is(1));
        }

        database.delete(FORMS_TABLE_NAME, null, null);
        database.insert(FORMS_TABLE_NAME, null, contentValues);
        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getCount(), is(1));
            cursor.moveToFirst();
            assertThat(cursor.getInt(cursor.getColumnIndex(_ID)), is(2));
        }
    }

    @Test
    public void onUpgrade_fromVersion13() {
        int oldVersion = 13;
        database.setVersion(oldVersion);
        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();

        formDatabaseMigrator.createFormsTableV13(database);
        ContentValues contentValues = getContentValuesForFormV13();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onUpgrade(database, oldVersion);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(19));
            assertThat(cursor.getCount(), is(1));

            cursor.moveToFirst();

            assertThat(cursor.getInt(cursor.getColumnIndex(_ID)), is(1));
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
            assertThat(cursor.getInt(cursor.getColumnIndex(DELETED_DATE)), is(contentValues.getAsInteger(DELETED_DATE)));
            assertThat(cursor.getInt(cursor.getColumnIndex(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)), is(contentValues.getAsInteger(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)));
            assertThat(cursor.getString(cursor.getColumnIndex(USES_ENTITIES)), is(nullValue()));
        }
    }

    @Test
    public void onUpgrade_fromVersion12() {
        int oldVersion = 12;
        database.setVersion(oldVersion);
        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();

        formDatabaseMigrator.createFormsTableV12(database);
        ContentValues contentValues = getContentValuesForFormV12();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onUpgrade(database, oldVersion);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(19));
            assertThat(cursor.getCount(), is(1));

            cursor.moveToFirst();

            assertThat(cursor.getInt(cursor.getColumnIndex(_ID)), is(1));
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
            assertThat(cursor.getInt(cursor.getColumnIndex(DELETED_DATE)), is(contentValues.getAsInteger(DELETED_DATE)));
            assertThat(cursor.getInt(cursor.getColumnIndex(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)), is(contentValues.getAsInteger(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)));
            assertThat(cursor.getString(cursor.getColumnIndex(USES_ENTITIES)), is(nullValue()));
        }
    }

    @Test
    public void onUpgrade_fromVersion11() {
        int oldVersion = 11;
        assertTrue(oldVersion < DatabaseConstants.FORMS_DATABASE_VERSION);
        database.setVersion(oldVersion);

        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
        formDatabaseMigrator.createFormsTableV11(database);

        ContentValues contentValues = createVersion11Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onUpgrade(database, oldVersion);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(19));
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
            assertThat(cursor.getInt(cursor.getColumnIndex(DELETED_DATE)), is(contentValues.getAsInteger(DELETED_DATE)));
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)), is(nullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(USES_ENTITIES)), is(nullValue()));
        }
    }

    @Test
    public void onUpgrade_fromVersion10() {
        int oldVersion = 10;
        assertTrue(oldVersion < DatabaseConstants.FORMS_DATABASE_VERSION);
        database.setVersion(oldVersion);

        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
        formDatabaseMigrator.createFormsTableV10(database);

        ContentValues contentValues = createVersion10Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onUpgrade(database, oldVersion);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(19));
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
            assertThat(cursor.getInt(cursor.getColumnIndex(DELETED_DATE)), is(contentValues.getAsInteger(DELETED_DATE)));
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)), is(nullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(USES_ENTITIES)), is(nullValue()));
        }
    }

    @Test
    public void onUpgrade_fromVersion9() {
        int oldVersion = 9;
        assertTrue(oldVersion < DatabaseConstants.FORMS_DATABASE_VERSION);
        database.setVersion(oldVersion);

        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
        formDatabaseMigrator.createFormsTableV9(database);

        ContentValues contentValues = createVersion9Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onUpgrade(database, oldVersion);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(19));
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
            assertThat(cursor.getInt(cursor.getColumnIndex(DELETED_DATE)), is(0));
            assertThat(cursor.getColumnIndex("deleted"), is(-1));
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)), is(nullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(USES_ENTITIES)), is(nullValue()));
        }
    }

    @Test
    public void onUpgrade_fromVersion8() {
        int oldVersion = 8;
        assertTrue(oldVersion < DatabaseConstants.FORMS_DATABASE_VERSION);
        database.setVersion(oldVersion);

        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
        formDatabaseMigrator.createFormsTableV8(database);

        ContentValues contentValues = createVersion8Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onUpgrade(database, oldVersion);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(19));
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
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)), is(nullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(USES_ENTITIES)), is(nullValue()));
        }
    }

    @Test
    public void onUpgrade_fromVersion7() {
        int oldVersion = 7;
        assertTrue(oldVersion < DatabaseConstants.FORMS_DATABASE_VERSION);
        database.setVersion(oldVersion);

        FormDatabaseMigrator formDatabaseMigrator = new FormDatabaseMigrator();
        formDatabaseMigrator.createFormsTableV7(database);

        ContentValues contentValues = createVersion7Form();
        database.insert(FORMS_TABLE_NAME, null, contentValues);

        formDatabaseMigrator.onUpgrade(database, oldVersion);

        try (Cursor cursor = database.rawQuery("SELECT * FROM " + FORMS_TABLE_NAME + ";", new String[]{})) {
            assertThat(cursor.getColumnCount(), is(19));
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
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)), is(nullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(USES_ENTITIES)), is(nullValue()));
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
        contentValues.put(LAST_DETECTED_FORM_VERSION_HASH, "LastDetectedFormVersionHash");
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
        contentValues.put(LAST_DETECTED_FORM_VERSION_HASH, "LastDetectedFormVersionHash");
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

    private ContentValues createVersion10Form() {
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
        contentValues.put(DELETED_DATE, 0);
        return contentValues;
    }

    private ContentValues createVersion11Form() {
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
        contentValues.put(DELETED_DATE, 0);
        return contentValues;
    }

    private ContentValues getContentValuesForFormV12() {
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
        contentValues.put(DELETED_DATE, 0);
        contentValues.put(LAST_DETECTED_ATTACHMENTS_UPDATE_DATE, 0);
        return contentValues;
    }

    private ContentValues getContentValuesForFormV13() {
        return getContentValuesForFormV12(); // there were no new columns added in v13
    }
}
