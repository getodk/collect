package org.odk.collect.android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.storage.StorageInitializer;
import org.robolectric.shadows.ShadowEnvironment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.odk.collect.android.instances.Instance.STATUS_COMPLETE;
import static org.odk.collect.android.instances.Instance.STATUS_INCOMPLETE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.CONTENT_URI;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DELETED_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.STATUS;

@RunWith(AndroidJUnit4.class)
public class InstanceProviderTest {

    private ContentResolver contentResolver;

    @Before
    public void setup() {
        // Fake that external storage is mounted (it isn't by default in Robolectric)
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        new StorageInitializer().createOdkDirsOnStorage();

        Context context = ApplicationProvider.getApplicationContext();
        contentResolver = context.getContentResolver();
    }

    @Test
    public void insert_addsInstance() {
        ContentValues values = getContentValues("/blah", "External app form", "external_app_form", "1");
        contentResolver.insert(CONTENT_URI, values);

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null)) {
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
        Uri uri = contentResolver.insert(CONTENT_URI, values);

        try (Cursor cursor = contentResolver.query(uri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));
        }
    }

    @Test
    public void update_updatesInstance_andReturns1() {
        ContentValues values = getContentValues("/blah", "External app form", "external_app_form", "1");

        long originalStatusChangeDate = 0L;
        values.put(LAST_STATUS_CHANGE_DATE, originalStatusChangeDate);
        Uri instanceUri = contentResolver.insert(CONTENT_URI, values);

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_COMPLETE);

        int updatedCount = contentResolver.update(instanceUri, updateValues, null, null);
        assertThat(updatedCount, is(1));
        try (Cursor cursor = contentResolver.query(instanceUri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(STATUS_COMPLETE));
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), is(not(originalStatusChangeDate)));
            assertThat(cursor.getString(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), is(notNullValue()));
        }
    }

    @Test
    public void update_whenDeletedDateIsIncluded_doesNotUpdateStatusChangeDate() {
        ContentValues values = getContentValues("/blah", "External app form", "external_app_form", "1");

        long originalStatusChangeDate = 0L;
        values.put(LAST_STATUS_CHANGE_DATE, originalStatusChangeDate);
        Uri instanceUri = contentResolver.insert(CONTENT_URI, values);

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
        addInstanceToDb("/blah1", "Instance 1");
        addInstanceToDb("/blah2", "Instance 2");

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_COMPLETE);
        contentResolver.update(CONTENT_URI, updateValues, INSTANCE_FILE_PATH + "=?", new String[]{"/blah2"});

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null)) {
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
        Uri uri = addInstanceToDb("/blah1", "Instance 1");
        addInstanceToDb("/blah1", "Instance 2");

        ContentValues updateValues = new ContentValues();
        updateValues.put(STATUS, STATUS_COMPLETE);
        contentResolver.update(uri, updateValues, DISPLAY_NAME + "=?", new String[]{"Instance 2"});

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null)) {
            assertThat(cursor.getCount(), is(2));

            while (cursor.moveToNext()) {
                assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(STATUS_INCOMPLETE));
            }
        }
    }

    private Uri addInstanceToDb(String s, String s2) {
        ContentValues values1 = getContentValues(s, s2, "external_app_form", "1");
        return contentResolver.insert(CONTENT_URI, values1);
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
