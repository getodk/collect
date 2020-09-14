/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.instrumented.settings;

import android.Manifest;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.ApplicationResetter;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResetAppStateTest {

    private final StoragePathProvider storagePathProvider = new StoragePathProvider();

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    @Before
    public void setUp() throws IOException {
        resetAppState(Arrays.asList(
                ApplicationResetter.ResetAction.RESET_PREFERENCES, ApplicationResetter.ResetAction.RESET_INSTANCES,
                ApplicationResetter.ResetAction.RESET_FORMS, ApplicationResetter.ResetAction.RESET_LAYERS,
                ApplicationResetter.ResetAction.RESET_CACHE, ApplicationResetter.ResetAction.RESET_OSM_DROID
        ));
    }

    @After
    public void tearDown() throws IOException {
        resetAppState(Arrays.asList(
                ApplicationResetter.ResetAction.RESET_PREFERENCES, ApplicationResetter.ResetAction.RESET_INSTANCES,
                ApplicationResetter.ResetAction.RESET_FORMS, ApplicationResetter.ResetAction.RESET_LAYERS,
                ApplicationResetter.ResetAction.RESET_CACHE, ApplicationResetter.ResetAction.RESET_OSM_DROID
        ));
    }

    @Test
    public void resetSettingsTest() throws IOException {
        WebCredentialsUtils webCredentialsUtils = new WebCredentialsUtils();
        webCredentialsUtils.saveCredentials("https://opendatakit.appspot.com/", "admin", "admin");

        setupTestSettings();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_PREFERENCES));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        assertEquals(settings.getString(GeneralKeys.KEY_USERNAME, ""), "");
        assertEquals(settings.getString(GeneralKeys.KEY_PASSWORD, ""), "");
        assertTrue(settings.getBoolean(AdminKeys.KEY_VIEW_SENT, true));

        assertEquals(0, getFormsCount());
        assertEquals(0, getInstancesCount());
        assertEquals("", webCredentialsUtils.getCredentials(URI.create("https://opendatakit.appspot.com/")).getUsername());
        assertEquals("", webCredentialsUtils.getCredentials(URI.create("https://opendatakit.appspot.com/")).getPassword());
    }

    @Test
    public void resetFormsTest() throws IOException {
        saveTestFormFiles();
        setupTestFormsDatabase();
        createTestItemsetsDatabaseFile();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_FORMS));
        assertFolderEmpty(storagePathProvider.getDirPath(StorageSubdirectory.FORMS));
        assertFalse(new File(storagePathProvider.getDirPath(StorageSubdirectory.METADATA) + "/itemsets.db").exists());
    }

    @Test
    public void resetInstancesTest() throws IOException {
        saveTestInstanceFiles();
        setupTestInstancesDatabase();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_INSTANCES));
        assertFolderEmpty(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES));
    }

    @Test
    public void resetLayersTest() throws IOException {
        saveTestLayerFiles();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_LAYERS));
        assertFolderEmpty(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS));
    }

    @Test
    public void resetCacheTest() throws IOException {
        saveTestCacheFiles();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_CACHE));
        assertFolderEmpty(storagePathProvider.getDirPath(StorageSubdirectory.CACHE));
    }

    @Test
    public void resetOSMDroidTest() throws IOException {
        saveTestOSMDroidFiles();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_OSM_DROID));
        assertFolderEmpty(Configuration.getInstance().getOsmdroidTileCache().getPath());
    }

    private void resetAppState(List<Integer> resetActions) {
        List<Integer> failedResetActions = new ApplicationResetter().reset(resetActions);
        assertEquals(0, failedResetActions.size());
    }

    private void setupTestSettings() throws IOException {
        String username = "usernameTest";
        String password = "passwordTest";
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        settings
                .edit()
                .putString(GeneralKeys.KEY_USERNAME, username)
                .putString(GeneralKeys.KEY_PASSWORD, password)
                .apply();

        assertEquals(username, settings.getString(GeneralKeys.KEY_USERNAME, null));
        assertEquals(password, settings.getString(GeneralKeys.KEY_PASSWORD, null));

        settings
                .edit()
                .putBoolean(AdminKeys.KEY_VIEW_SENT, false)
                .apply();

        assertFalse(settings.getBoolean(AdminKeys.KEY_VIEW_SENT, false));

        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.SETTINGS)).exists() || new File(storagePathProvider.getDirPath(StorageSubdirectory.SETTINGS)).mkdir());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.SETTINGS) + "/collect.settings").createNewFile());
        assertTrue(new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings").createNewFile());
    }

    private void setupTestFormsDatabase() {
        ContentValues values = new ContentValues();
        values.put(FormsColumns.JRCACHE_FILE_PATH, storagePathProvider.getCacheDbPath("3a76a386464925b6f3e53422673dfe3c.formdef"));
        values.put(FormsColumns.JR_FORM_ID, "jrFormId");
        values.put(FormsColumns.FORM_MEDIA_PATH, storagePathProvider.getFormDbPath("testFile1-media"));
        values.put(FormsColumns.DATE, "1487077903756");
        values.put(FormsColumns.DISPLAY_NAME, "displayName");
        values.put(FormsColumns.FORM_FILE_PATH, storagePathProvider.getFormDbPath("testFile1.xml"));
        Collect.getInstance().getContentResolver()
                .insert(FormsColumns.CONTENT_URI, values);

        assertEquals(1, getFormsCount());
    }

    private void setupTestInstancesDatabase() {
        ContentValues values = new ContentValues();
        values.put(InstanceColumns.INSTANCE_FILE_PATH, storagePathProvider.getInstanceDbPath("testDir1/testFile1"));
        values.put(InstanceColumns.SUBMISSION_URI, "submissionUri");
        values.put(InstanceColumns.DISPLAY_NAME, "displayName");
        values.put(InstanceColumns.DISPLAY_NAME, "formName");
        values.put(InstanceColumns.JR_FORM_ID, "jrformid");
        values.put(InstanceColumns.JR_VERSION, "jrversion");
        Collect.getInstance().getContentResolver()
                .insert(InstanceColumns.CONTENT_URI, values);

        assertEquals(1, getInstancesCount());
    }

    private void createTestItemsetsDatabaseFile() throws IOException {
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.METADATA) + "/itemsets.db").createNewFile());
    }

    private void saveTestFormFiles() throws IOException {
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + "/testFile1.xml").createNewFile());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + "/testFile2.xml").createNewFile());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + "/testFile3.xml").createNewFile());

        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + "/testDir1/testFile1-media").mkdirs());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + "/testDir2/testFile2-media").mkdirs());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + "/testDir3/testFile3-media/testFile.csv").mkdirs());
    }

    private void saveTestInstanceFiles() {
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES) + "/testDir1/testFile1.xml").mkdirs());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES) + "/testDir2/testFile2.xml").mkdirs());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES) + "/testDir3").mkdirs());
    }

    private void saveTestLayerFiles() throws IOException {
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS) + "/testFile1").createNewFile());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS) + "/testFile2").createNewFile());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS) + "/testFile3").createNewFile());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS) + "/testFile4").createNewFile());
    }

    private void saveTestCacheFiles() throws IOException {
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.CACHE) + "/testFile1").createNewFile());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.CACHE) + "/testFile2").createNewFile());
        assertTrue(new File(storagePathProvider.getDirPath(StorageSubdirectory.CACHE) + "/testFile3").createNewFile());
    }

    private void saveTestOSMDroidFiles() throws IOException {
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile1").mkdirs());
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile2").mkdirs());
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile3").mkdirs());
    }

    private int getFormsCount() {
        int forms = 0;
        Cursor cursor = Collect.getInstance().getContentResolver().query(
                FormsColumns.CONTENT_URI, null, null, null,
                FormsColumns.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        forms++;
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return forms;
    }

    private int getInstancesCount() {
        int instances = 0;
        Cursor cursor = Collect.getInstance().getContentResolver().query(
                InstanceColumns.CONTENT_URI, null, null, null,
                InstanceColumns.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        instances++;
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return instances;
    }

    private void assertFolderEmpty(String folder) {
        assertTrue(new File(folder).isDirectory());
        assertEquals(new File(folder).list().length, 0);
    }
}
