package org.odk.collect.android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.FormUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.CONTENT_ITEM_TYPE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.CONTENT_TYPE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.CONTENT_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LANGUAGE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;

@RunWith(AndroidJUnit4.class)
public class FormsProviderTest {

    private ContentResolver contentResolver;
    private File externalFilesDir;

    @Before
    public void setup() {
        // Fake that external storage is mounted (it isn't by default in Robolectric)
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        Context context = ApplicationProvider.getApplicationContext();
        externalFilesDir = context.getExternalFilesDir(null);
        contentResolver = context.getContentResolver();
    }

    @Test
    public void insert_addsForm() {
        String formId = "external_app_form";
        String formVersion = "1";
        String formName = "External app form";
        File formFile = addFormToFormsDir(formId, formVersion, formName);
        String md5Hash = FileUtils.getMd5Hash(formFile);

        ContentValues values = getContentValues(formId, formVersion, formName, formFile);
        contentResolver.insert(CONTENT_URI, values);

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is(formName));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(formId));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is(formVersion));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH)), is(formFile.getName()));

            assertThat(cursor.getString(cursor.getColumnIndex(DATE)), is(notNullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(MD5_HASH)), is(md5Hash));
            assertThat(cursor.getString(cursor.getColumnIndex(JRCACHE_FILE_PATH)), is(md5Hash + ".formdef"));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH)), is(formFile.getName().substring(0, formFile.getName().lastIndexOf(".")) + "-media"));
        }
    }

    @Test
    public void insert_returnsFormUri() {
        String formId = "external_app_form";
        String formVersion = "1";
        String formName = "External app form";
        File formFile = addFormToFormsDir(formId, formVersion, formName);
        String md5Hash = FileUtils.getMd5Hash(formFile);

        ContentValues values = getContentValues(formId, formVersion, formName, formFile);
        Uri newFormUri = contentResolver.insert(CONTENT_URI, values);

        try (Cursor cursor = contentResolver.query(newFormUri, null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is(formName));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(formId));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is(formVersion));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH)), is(formFile.getName()));

            assertThat(cursor.getString(cursor.getColumnIndex(DATE)), is(notNullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(MD5_HASH)), is(md5Hash));
            assertThat(cursor.getString(cursor.getColumnIndex(JRCACHE_FILE_PATH)), is(md5Hash + ".formdef"));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH)), is(formFile.getName().substring(0, formFile.getName().lastIndexOf(".")) + "-media"));
        }
    }

    @Test
    public void update_updatesForm() {
        Uri formUri = addFormsToDirAndDb("external_app_form", "1", "External app form");

        ContentValues contentValues = new ContentValues();
        contentValues.put(LANGUAGE, "English");

        contentResolver.update(formUri, contentValues, null, null);
        try (Cursor cursor = contentResolver.query(formUri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(LANGUAGE)), is("English"));
        }
    }

    @Test
    public void update_withSelection_onlyUpdatesMatchingForms() {
        addFormsToDirAndDb("form1", "1", "Matching form");
        addFormsToDirAndDb("form2", "1", "Not matching form");
        addFormsToDirAndDb("form3", "1", "Matching form");

        ContentValues contentValues = new ContentValues();
        contentValues.put(LANGUAGE, "English");

        contentResolver.update(CONTENT_URI, contentValues, DISPLAY_NAME + "=?", new String[]{"Matching form"});
        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null)) {
            assertThat(cursor.getCount(), is(3));

            cursor.moveToNext();
            if (cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)).equals("Matching form")) {
                assertThat(cursor.getString(cursor.getColumnIndex(LANGUAGE)), is("English"));
            } else {
                assertThat(cursor.isNull(cursor.getColumnIndex(LANGUAGE)), is(true));
            }
        }
    }

    @Test
    public void delete_deletesForm() {
        Uri formUri = addFormsToDirAndDb("form1", "1", "Matching form");
        contentResolver.delete(formUri, null, null);

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null)) {
            assertThat(cursor.getCount(), is(0));
        }
    }

    @Test
    public void delete_deletesFiles() {
        Uri formUri = addFormsToDirAndDb("form1", "1", "Matching form");
        try (Cursor cursor = contentResolver.query(formUri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            String formFileName = cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH));
            File formFile = new File(getFormsDirPath() + formFileName);
            assertThat(formFile.exists(), is(true));

            String mediaDirName = cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH));
            File mediaDir = new File(getFormsDirPath() + mediaDirName);
            assertThat(mediaDir.exists(), is(true));

            contentResolver.delete(formUri, null, null);
            assertThat(formFile.exists(), is(false));
            assertThat(mediaDir.exists(), is(false));
        }
    }

    @Test
    public void delete_withSelection_onlyDeletesMatchingForms() {
        addFormsToDirAndDb("form1", "1", "Matching form");
        addFormsToDirAndDb("form2", "1", "Not matching form");
        addFormsToDirAndDb("form3", "1", "Matching form");

        contentResolver.delete(CONTENT_URI, DISPLAY_NAME + "=?", new String[]{"Matching form"});
        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("form2"));
        }
    }

    @Test
    public void query_withProjection_onlyReturnsSpecifiedColumns() {
        addFormsToDirAndDb("external_app_form", "1", "External app form");

        try (Cursor cursor = contentResolver.query(CONTENT_URI, new String[]{JR_FORM_ID, JR_VERSION}, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getColumnCount(), is(2));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("external_app_form"));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is("1"));
        }
    }

    @Test
    public void query_withSelection_onlyReturnsMatchingRows() {
        addFormsToDirAndDb("form1", "1", "Matching form");
        addFormsToDirAndDb("form2", "1", "Not a matching form");
        addFormsToDirAndDb("form3", "1", "Matching form");

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, DISPLAY_NAME + "=?", new String[]{"Matching form"}, null)) {
            assertThat(cursor.getCount(), is(2));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(isOneOf("form1", "form3")));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(isOneOf("form1", "form3")));
        }
    }

    @Test
    public void query_withSortOrder_returnsSortedResults() {
        addFormsToDirAndDb("formB", "1", "Form B");
        addFormsToDirAndDb("formC", "1", "Form C");
        addFormsToDirAndDb("formA", "1", "Form A");

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, DISPLAY_NAME + " ASC")) {
            assertThat(cursor.getCount(), is(3));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("formA"));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("formB"));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("formC"));
        }
    }

    @Test
    public void getType_returnsFormAndAllFormsTypes() {
        assertThat(contentResolver.getType(CONTENT_URI), is(CONTENT_TYPE));
        assertThat(contentResolver.getType(Uri.withAppendedPath(CONTENT_URI, "1")), is(CONTENT_ITEM_TYPE));
    }

    private Uri addFormsToDirAndDb(String id, String version, String name) {
        File formFile = addFormToFormsDir(id, version, name);
        ContentValues values = getContentValues(id, version, name, formFile);
        return contentResolver.insert(CONTENT_URI, values);
    }

    @NotNull
    private ContentValues getContentValues(String formId, String formVersion, String formName, File formFile) {
        ContentValues values = new ContentValues();
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, formName);
        values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, formId);
        values.put(FormsProviderAPI.FormsColumns.JR_VERSION, formVersion);
        values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, formFile.getAbsolutePath());
        return values;
    }

    /**
     * It seems like newer OS versions (10 or higher) won't let other apps actually do this as they won't be
     * able to access our external files dir (according to
     * https://developer.android.com/training/data-storage/app-specific#external anyway.
     **/
    private File addFormToFormsDir(String formId, String formVersion, String formName) {
        String xformBody = FormUtils.createXFormBody(formId, formVersion, formName);
        String fileName = formId + "-" + formVersion + "-" + Math.random();
        File formFile = new File(getFormsDirPath() + fileName + ".xml");
        FileUtils.write(formFile, xformBody.getBytes());

        String mediaDirPath = getFormsDirPath() + formFile.getName().substring(0, formFile.getName().lastIndexOf(".")) + "-media";
        new File(mediaDirPath).mkdir();

        return formFile;
    }

    @NotNull
    private String getFormsDirPath() {
        return externalFilesDir + File.separator + "forms" + File.separator;
    }
}
