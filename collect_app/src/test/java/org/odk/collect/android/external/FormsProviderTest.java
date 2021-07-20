package org.odk.collect.android.external;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.formstest.FormUtils;
import org.odk.collect.projects.Project;
import org.odk.collect.shared.strings.Md5;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_FORM_ID;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_VERSION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.LANGUAGE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.MD5_HASH;
import static org.odk.collect.android.external.FormsContract.CONTENT_ITEM_TYPE;
import static org.odk.collect.android.external.FormsContract.CONTENT_TYPE;
import static org.odk.collect.android.external.FormsContract.getUri;

@RunWith(AndroidJUnit4.class)
public class FormsProviderTest {

    private ContentResolver contentResolver;
    private StoragePathProvider storagePathProvider;
    private String firstProjectId;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        storagePathProvider = DaggerUtils.getComponent(context).storagePathProvider();

        firstProjectId = CollectHelpers.createDemoProject();
        contentResolver = context.getContentResolver();
    }

    @Test
    public void insert_addsForm() {
        String formId = "external_app_form";
        String formVersion = "1";
        String formName = "External app form";
        File formFile = addFormToFormsDir(firstProjectId, formId, formVersion, formName);
        String md5Hash = Md5.getMd5Hash(formFile);

        ContentValues values = getContentValues(formId, formVersion, formName, formFile);
        contentResolver.insert(getUri(firstProjectId), values);

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is(formName));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(formId));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is(formVersion));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH)), is(formFile.getName()));

            assertThat(cursor.getString(cursor.getColumnIndex(DATE)), is(notNullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(MD5_HASH)), is(md5Hash));
            assertThat(cursor.getString(cursor.getColumnIndex(JRCACHE_FILE_PATH)), is(md5Hash + ".formdef"));
            assertThat(cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH)), is(mediaPathForFormFile(formFile)));
        }
    }

    @Test
    public void insert_returnsFormUri() {
        String formId = "external_app_form";
        String formVersion = "1";
        String formName = "External app form";
        File formFile = addFormToFormsDir(firstProjectId, formId, formVersion, formName);

        ContentValues values = getContentValues(formId, formVersion, formName, formFile);
        Uri newFormUri = contentResolver.insert(getUri(firstProjectId), values);

        try (Cursor cursor = contentResolver.query(newFormUri, null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));
        }
    }

    @Test
    public void update_updatesForm_andReturns1() {
        Uri formUri = addFormsToDirAndDb(firstProjectId, "external_app_form", "External app form", "1");

        ContentValues contentValues = new ContentValues();
        contentValues.put(LANGUAGE, "English");

        int updateCount = contentResolver.update(formUri, contentValues, null, null);
        assertThat(updateCount, is(1));
        try (Cursor cursor = contentResolver.query(formUri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(LANGUAGE)), is("English"));
        }
    }

    @Test
    public void update_withSelection_onlyUpdatesMatchingForms() {
        addFormsToDirAndDb(firstProjectId, "form1", "Matching form", "1");
        addFormsToDirAndDb(firstProjectId, "form2", "Not matching form", "1");
        addFormsToDirAndDb(firstProjectId, "form3", "Matching form", "1");

        ContentValues contentValues = new ContentValues();
        contentValues.put(LANGUAGE, "English");

        contentResolver.update(getUri(firstProjectId), contentValues, DISPLAY_NAME + "=?", new String[]{"Matching form"});
        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null)) {
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
    public void update_whenFormDoesNotExist_returns0() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LANGUAGE, "English");

        int updatedCount = contentResolver.update(Uri.withAppendedPath(getUri(firstProjectId), String.valueOf(1)), contentValues, null, null);
        assertThat(updatedCount, is(0));
    }

    @Test
    public void delete_deletesForm() {
        Uri formUri = addFormsToDirAndDb(firstProjectId, "form1", "Matching form", "1");
        contentResolver.delete(formUri, null, null);

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null)) {
            assertThat(cursor.getCount(), is(0));
        }
    }

    @Test
    public void delete_deletesFiles() {
        Uri formUri = addFormsToDirAndDb(firstProjectId, "form1", "Matching form", "1");
        try (Cursor cursor = contentResolver.query(formUri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            String formFileName = cursor.getString(cursor.getColumnIndex(FORM_FILE_PATH));
            File formFile = new File(getFormsDirPath(firstProjectId) + formFileName);
            assertThat(formFile.exists(), is(true));

            String mediaDirName = cursor.getString(cursor.getColumnIndex(FORM_MEDIA_PATH));
            File mediaDir = new File(getFormsDirPath(firstProjectId) + mediaDirName);
            assertThat(mediaDir.exists(), is(true));

            String cacheFileName = cursor.getString(cursor.getColumnIndex(JRCACHE_FILE_PATH));
            File cacheFile = new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, firstProjectId) + File.separator + cacheFileName);
            assertThat(cacheFile.exists(), is(true));

            contentResolver.delete(formUri, null, null);

            assertThat(formFile.exists(), is(false));
            assertThat(mediaDir.exists(), is(false));
            assertThat(cacheFile.exists(), is(false));
        }
    }

    @Test
    public void delete_withSelection_onlyDeletesMatchingForms() {
        addFormsToDirAndDb(firstProjectId, "form1", "Matching form", "1");
        addFormsToDirAndDb(firstProjectId, "form2", "Not matching form", "1");
        addFormsToDirAndDb(firstProjectId, "form3", "Matching form", "1");

        contentResolver.delete(getUri(firstProjectId), DISPLAY_NAME + "=?", new String[]{"Matching form"});
        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("form2"));
        }
    }

    @Test
    public void query_returnsTheExpectedNumberColumns() {
        Uri uri = addFormsToDirAndDb(firstProjectId, "external_app_form", "External app form", "1");

        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            assertThat(cursor.getColumnCount(), is(17));
        }
    }

    @Test
    public void query_withProjection_onlyReturnsSpecifiedColumns() {
        addFormsToDirAndDb(firstProjectId, "external_app_form", "External app form", "1");

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), new String[]{JR_FORM_ID, JR_VERSION}, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getColumnCount(), is(2));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("external_app_form"));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is("1"));
        }
    }

    @Test
    public void query_withSelection_onlyReturnsMatchingRows() {
        addFormsToDirAndDb(firstProjectId, "form1", "Matching form", "1");
        addFormsToDirAndDb(firstProjectId, "form2", "Not a matching form", "1");
        addFormsToDirAndDb(firstProjectId, "form3", "Matching form", "1");

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, DISPLAY_NAME + "=?", new String[]{"Matching form"}, null)) {
            assertThat(cursor.getCount(), is(2));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(isOneOf("form1", "form3")));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is(isOneOf("form1", "form3")));
        }
    }

    @Test
    public void query_withSortOrder_returnsSortedResults() {
        addFormsToDirAndDb(firstProjectId, "formB", "Form B", "1");
        addFormsToDirAndDb(firstProjectId, "formC", "Form C", "1");
        addFormsToDirAndDb(firstProjectId, "formA", "Form A", "1");

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, DISPLAY_NAME + " ASC")) {
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
    public void query_withoutProjectId_usesFirstProject() {
        addFormsToDirAndDb(firstProjectId, "formA", "Form A", "1");
        CollectHelpers.createProject(new Project.New("Another Project", "A", "#ffffff"));

        Uri uriWithProject = getUri("blah");
        Uri uriWithoutProject = new Uri.Builder()
                .scheme(uriWithProject.getScheme())
                .authority(uriWithProject.getAuthority())
                .path(uriWithProject.getPath())
                .query(null)
                .build();

        try (Cursor cursor = contentResolver.query(uriWithoutProject, null, null, null, DISPLAY_NAME + " ASC")) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("formA"));
        }
    }

    @Test
    public void getType_returnsFormAndAllFormsTypes() {
        assertThat(contentResolver.getType(getUri(firstProjectId)), is(CONTENT_TYPE));
        assertThat(contentResolver.getType(Uri.withAppendedPath(getUri(firstProjectId), "1")), is(CONTENT_ITEM_TYPE));
    }

    private Uri addFormsToDirAndDb(String projectId, String id, String name, String version) {
        File formFile = addFormToFormsDir(projectId, id, version, name);
        ContentValues values = getContentValues(id, version, name, formFile);
        return contentResolver.insert(getUri(projectId), values);
    }

    @NotNull
    private ContentValues getContentValues(String formId, String formVersion, String formName, File formFile) {
        ContentValues values = new ContentValues();
        values.put(DISPLAY_NAME, formName);
        values.put(JR_FORM_ID, formId);
        values.put(JR_VERSION, formVersion);
        values.put(FORM_FILE_PATH, formFile.getAbsolutePath());
        return values;
    }

    /**
     * It seems like newer OS versions (10 or higher) won't let other apps actually do this as they won't be
     * able to access our external files dir (according to
     * https://developer.android.com/training/data-storage/app-specific#external anyway.
     **/
    private File addFormToFormsDir(String projectId, String formId, String formVersion, String formName) {
        File formFile = createFormFileInFormsDir(projectId, formId, formVersion, formName);
        String md5Hash = Md5.getMd5Hash(formFile);

        createExtraFormFiles(projectId, formFile, md5Hash);
        return formFile;
    }

    private File createFormFileInFormsDir(String projectId, String formId, String formVersion, String formName) {
        String xformBody = FormUtils.createXFormBody(formId, formVersion, formName);
        String fileName = formId + "-" + formVersion + "-" + Math.random();
        File formFile = new File(getFormsDirPath(projectId) + fileName + ".xml");
        FileUtils.write(formFile, xformBody.getBytes());
        return formFile;
    }

    private void createExtraFormFiles(String projectId, File formFile, String md5Hash) {
        // Create a media directory (and file) so we can check deletion etc - wouldn't always be there
        String mediaDirPath = getFormsDirPath(projectId) + formFile.getName().substring(0, formFile.getName().lastIndexOf(".")) + "-media";
        new File(mediaDirPath).mkdir();
        try {
            new File(mediaDirPath, "blah.test").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create a cache file so we can check deletion etc - wouldn't always be there
        try {
            new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId) + File.separator + md5Hash + ".formdef").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String mediaPathForFormFile(File newFile) {
        return newFile.getName().substring(0, newFile.getName().lastIndexOf(".")) + "-media";
    }

    @NotNull
    private String getFormsDirPath(String projectId) {
        return storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId) + File.separator;
    }
}
