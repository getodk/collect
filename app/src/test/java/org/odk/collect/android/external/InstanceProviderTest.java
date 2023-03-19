package org.odk.collect.android.external;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.database.forms.DatabaseFormColumns;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.formstest.InstanceUtils;
import org.odk.collect.projects.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DELETED_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY_TYPE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_VERSION;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.STATUS;
import static org.odk.collect.android.external.InstancesContract.CONTENT_ITEM_TYPE;
import static org.odk.collect.android.external.InstancesContract.CONTENT_TYPE;
import static org.odk.collect.android.external.InstancesContract.getUri;
import static org.odk.collect.forms.instances.Instance.STATUS_COMPLETE;
import static org.odk.collect.forms.instances.Instance.STATUS_INCOMPLETE;
import static org.odk.collect.forms.instances.Instance.STATUS_SUBMITTED;

@RunWith(AndroidJUnit4.class)
public class InstanceProviderTest {

    private ContentResolver contentResolver;
    private String firstProjectId;
    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        storagePathProvider = DaggerUtils.getComponent(context).storagePathProvider();

        firstProjectId = CollectHelpers.createDemoProject();
        contentResolver = context.getContentResolver();
    }

    @Test
    public void insert_addsInstance() {
        ContentValues values = getContentValues("/blah", "External app form", "external_app_form", "1");
        contentResolver.insert(getUri(firstProjectId), values);

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(INSTANCE_FILE_PATH)), is("/blah"));
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("External app form"));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("external_app_form"));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is("1"));

            assertThat(cursor.getLong(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), is(notNullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(STATUS_INCOMPLETE));
        }
    }

    @Test
    public void insert_returnsInstanceUri() {
        ContentValues values = getContentValues("/blah", "External app form", "external_app_form", "1");
        Uri uri = contentResolver.insert(getUri(firstProjectId), values);

        try (Cursor cursor = contentResolver.query(uri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));
        }
    }

    @Test
    public void update_updatesInstance_andReturns1() {
        ContentValues values = getContentValues("/blah", "External app form", "external_app_form", "1");

        long originalStatusChangeDate = 0L;
        values.put(LAST_STATUS_CHANGE_DATE, originalStatusChangeDate);
        Uri instanceUri = contentResolver.insert(getUri(firstProjectId), values);

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_COMPLETE);

        int updatedCount = contentResolver.update(instanceUri, updateValues, null, null);
        assertThat(updatedCount, is(1));
        try (Cursor cursor = contentResolver.query(instanceUri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(STATUS_COMPLETE));
            assertThat(cursor.getLong(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), is(not(originalStatusChangeDate)));
            assertThat(cursor.getLong(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), is(notNullValue()));
        }
    }

    @Test
    public void update_whenDeletedDateIsIncluded_doesNotUpdateStatusChangeDate() {
        ContentValues values = getContentValues("/blah", "External app form", "external_app_form", "1");

        long originalStatusChangeDate = 0L;
        values.put(LAST_STATUS_CHANGE_DATE, originalStatusChangeDate);
        Uri instanceUri = contentResolver.insert(getUri(firstProjectId), values);

        ContentValues updateValues = new ContentValues();
        updateValues.put(DELETED_DATE, 123L);

        contentResolver.update(instanceUri, updateValues, null, null);
        try (Cursor cursor = contentResolver.query(instanceUri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getLong(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), is(originalStatusChangeDate));
        }
    }

    @Test
    public void update_withSelection_onlyUpdatesMatchingInstance() {
        addInstanceToDb(firstProjectId, "/blah1", "Instance 1");
        addInstanceToDb(firstProjectId, "/blah2", "Instance 2");

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_COMPLETE);
        contentResolver.update(getUri(firstProjectId), updateValues, INSTANCE_FILE_PATH + "=?", new String[]{"/blah2"});

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(2));

            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex(INSTANCE_FILE_PATH)).equals("/blah2")) {
                    assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(STATUS_COMPLETE));
                } else {
                    assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(STATUS_INCOMPLETE));
                }
            }
        }
    }

    /**
     * It's not clear when this is used. A hypothetical might be updating an instance but wanting
     * that to be a no-op if it has already been soft deleted.
     */
    @Test
    public void update_withInstanceUri_andSelection_doesNotUpdateInstanceThatDoesNotMatchSelection() {
        Uri uri = addInstanceToDb(firstProjectId, "/blah1", "Instance 1");
        addInstanceToDb(firstProjectId, "/blah1", "Instance 2");

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_COMPLETE);
        contentResolver.update(uri, updateValues, DISPLAY_NAME + "=?", new String[]{"Instance 2"});

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(2));

            while (cursor.moveToNext()) {
                assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(STATUS_INCOMPLETE));
            }
        }
    }

    @Test
    public void delete_deletesInstance() {
        Uri uri = addInstanceToDb(firstProjectId, "/blah1", "Instance 1");
        contentResolver.delete(uri, null, null);

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(0));
        }
    }

    @Test
    public void delete_deletesInstanceDir() {
        File instanceFile = createInstanceDirAndFile(firstProjectId);

        Uri uri = addInstanceToDb(firstProjectId, instanceFile.getAbsolutePath(), "Instance 1");
        contentResolver.delete(uri, null, null);
        assertThat(instanceFile.getParentFile().exists(), is(false));
    }

    @Test
    public void delete_whenStatusIsSubmitted_deletesFilesButSoftDeletesInstance() {
        File instanceFile = createInstanceDirAndFile(firstProjectId);
        Uri uri = addInstanceToDb(firstProjectId, instanceFile.getAbsolutePath(), "Instance 1");

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_SUBMITTED);
        contentResolver.update(uri, updateValues, null, null);

        contentResolver.delete(uri, null, null);
        assertThat(instanceFile.getParentFile().exists(), is(false));

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getLong(cursor.getColumnIndex(DELETED_DATE)), is(notNullValue()));
        }
    }

    @Test
    public void delete_whenStatusIsSubmitted_clearsGeometryFields() {
        File instanceFile = createInstanceDirAndFile(firstProjectId);
        Uri uri = addInstanceToDb(firstProjectId, instanceFile.getAbsolutePath(), "Instance 1");

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_SUBMITTED);
        updateValues.put(GEOMETRY, "something");
        updateValues.put(GEOMETRY_TYPE, "something else");
        contentResolver.update(uri, updateValues, null, null);

        contentResolver.delete(uri, null, null);
        assertThat(instanceFile.getParentFile().exists(), is(false));

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY)), is(nullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(GEOMETRY_TYPE)), is(nullValue()));
        }
    }

    /**
     * It's not clear when this is used. A hypothetical might be updating an instance but wanting
     * that to be a no-op if it has already been soft deleted.
     */
    @Test
    public void delete_withInstanceUri_andSelection_doesNotDeleteInstanceThatDoesNotMatchSelection() {
        Uri uri = addInstanceToDb(firstProjectId, "/blah1", "Instance 1");
        addInstanceToDb(firstProjectId, "/blah2", "Instance 2");

        contentResolver.delete(uri, DISPLAY_NAME + "=?", new String[]{"Instance 2"});

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(2));
        }
    }

    @Test
    public void delete_withSelection_deletesMatchingInstances() {
        addInstanceToDb(firstProjectId, "/blah1", "Instance 1");
        addInstanceToDb(firstProjectId, "/blah2", "Instance 2");

        contentResolver.delete(getUri(firstProjectId), DISPLAY_NAME + "=?", new String[]{"Instance 2"});
        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("Instance 1"));
        }
    }

    @Test
    public void query_returnsTheExpectedNumberColumns() {
        Uri uri = addInstanceToDb(firstProjectId, "/blah1", "Instance 1");

        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            assertThat(cursor.getColumnCount(), is(12));
        }
    }

    @Test
    public void query_withProjection_onlyReturnsSpecifiedColumns() {
        addInstanceToDb(firstProjectId, "/blah1", "Instance 1");

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), new String[]{INSTANCE_FILE_PATH, DISPLAY_NAME}, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getColumnCount(), is(2));
            assertThat(cursor.getString(cursor.getColumnIndex(INSTANCE_FILE_PATH)), is("/blah1"));
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("Instance 1"));
        }
    }

    @Test
    public void query_withSelection_onlyReturnsMatchingRows() {
        addInstanceToDb(firstProjectId, "/blah1", "Matching instance");
        addInstanceToDb(firstProjectId, "/blah2", "Not a matching instance");
        addInstanceToDb(firstProjectId, "/blah3", "Matching instance");

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, DISPLAY_NAME + "=?", new String[]{"Matching instance"}, null)) {
            assertThat(cursor.getCount(), is(2));

            List<String> paths = new ArrayList<>();
            while (cursor.moveToNext()) {
                paths.add(cursor.getString(cursor.getColumnIndex(INSTANCE_FILE_PATH)));
            }

            assertThat(paths, contains("/blah1", "/blah3"));
        }
    }

    @Test
    public void query_withSortOrder_returnsSortedResults() {
        addInstanceToDb(firstProjectId, "/blah3", "Instance C");
        addInstanceToDb(firstProjectId, "/blah2", "Instance B");
        addInstanceToDb(firstProjectId, "/blah1", "Instance A");

        try (Cursor cursor = contentResolver.query(getUri(firstProjectId), null, null, null, DISPLAY_NAME + " ASC")) {
            assertThat(cursor.getCount(), is(3));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("Instance A"));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("Instance B"));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("Instance C"));
        }
    }

    @Test
    public void query_withoutProjectId_usesFirstProject() {
        addInstanceToDb(firstProjectId, "/blah1", "Instance A");
        CollectHelpers.createProject(new Project.New("Another Project", "A", "#ffffff"));

        Uri uriWithProject = InstancesContract.getUri("blah");
        Uri uriWithoutProject = new Uri.Builder()
                .scheme(uriWithProject.getScheme())
                .authority(uriWithProject.getAuthority())
                .path(uriWithProject.getPath())
                .query(null)
                .build();

        try (Cursor cursor = contentResolver.query(uriWithoutProject, null, null, null, DatabaseFormColumns.DISPLAY_NAME + " ASC")) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("Instance A"));
        }
    }

    @Test
    public void getType_returnsInstanceAndAllInstanceTypes() {
        assertThat(contentResolver.getType(getUri(firstProjectId)), is(CONTENT_TYPE));
        assertThat(contentResolver.getType(Uri.withAppendedPath(getUri(firstProjectId), "1")), is(CONTENT_ITEM_TYPE));
    }

    private File createInstanceDirAndFile(String projectId) {
        return InstanceUtils.createInstanceDirAndFile(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId));
    }

    private Uri addInstanceToDb(String projectId, String instanceFilePath, String displayName) {
        ContentValues values = getContentValues(instanceFilePath, displayName, "external_app_form", "1");
        return contentResolver.insert(getUri(projectId), values);
    }

    private ContentValues getContentValues(String instanceFilePath, String displayName, String formId, String formVersion) {
        ContentValues values = new ContentValues();
        values.put(INSTANCE_FILE_PATH, instanceFilePath);
        values.put(DISPLAY_NAME, displayName);
        values.put(JR_FORM_ID, formId);
        values.put(JR_VERSION, formVersion);
        return values;
    }
}
