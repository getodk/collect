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
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.storage.StorageInitializer;
import org.robolectric.shadows.ShadowEnvironment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.CONTENT_URI;
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
        ContentValues values = new ContentValues();
        values.put(INSTANCE_FILE_PATH, "/blah");
        values.put(DISPLAY_NAME, "External app form");
        values.put(JR_FORM_ID, "external_app_form");
        values.put(JR_VERSION, "1");

        contentResolver.insert(CONTENT_URI, values);

        try (Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null)) {
            assertThat(cursor.getCount(), is(1));

            cursor.moveToNext();
            assertThat(cursor.getString(cursor.getColumnIndex(INSTANCE_FILE_PATH)), is("/blah"));
            assertThat(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)), is("External app form"));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_FORM_ID)), is("external_app_form"));
            assertThat(cursor.getString(cursor.getColumnIndex(JR_VERSION)), is("1"));

            assertThat(cursor.getLong(cursor.getColumnIndex(LAST_STATUS_CHANGE_DATE)), is(notNullValue()));
            assertThat(cursor.getString(cursor.getColumnIndex(STATUS)), is(Instance.STATUS_INCOMPLETE));
        }
    }

    @Test
    public void insert_returnsInstanceUri() {
        ContentValues values = new ContentValues();
        values.put(INSTANCE_FILE_PATH, "/blah");
        values.put(DISPLAY_NAME, "External app form");
        values.put(JR_FORM_ID, "external_app_form");
        values.put(JR_VERSION, "1");

        Uri uri = contentResolver.insert(CONTENT_URI, values);

        try (Cursor cursor = contentResolver.query(uri, null, null, null)) {
            assertThat(cursor.getCount(), is(1));
        }
    }
}
