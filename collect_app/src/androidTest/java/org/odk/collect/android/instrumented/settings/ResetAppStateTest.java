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

import android.content.ContentValues;
import android.database.Cursor;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseInstancesRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.Settings;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResetAppStateTest {

    private final StoragePathProvider storagePathProvider = new StoragePathProvider();
    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();

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
        WebCredentialsUtils webCredentialsUtils = new WebCredentialsUtils(generalSettings);
        webCredentialsUtils.saveCredentials("https://demo.getodk.org", "admin", "admin");

        setupTestSettings();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_PREFERENCES));

        assertEquals(generalSettings.getString(GeneralKeys.KEY_USERNAME), "");
        assertEquals(generalSettings.getString(GeneralKeys.KEY_PASSWORD), "");
        assertTrue(adminSettings.getBoolean(AdminKeys.KEY_VIEW_SENT));

        assertEquals(0, getFormsCount());
        assertEquals(0, getInstancesCount());
        assertEquals("", webCredentialsUtils.getCredentials(URI.create("https://demo.getodk.org")).getUsername());
        assertEquals("", webCredentialsUtils.getCredentials(URI.create("https://demo.getodk.org")).getPassword());
    }

    @Test
    public void resetFormsTest() throws IOException {
        saveTestFormFiles();
        setupTestFormsDatabase();
        createTestItemsetsDatabaseFile();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_FORMS));
        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS));
        assertFalse(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA) + "/itemsets.db").exists());
    }

    @Test
    public void resetInstancesTest() throws IOException {
        saveTestInstanceFiles();
        setupTestInstancesDatabase();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_INSTANCES));
        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES));
    }

    @Test
    public void resetLayersTest() throws IOException {
        saveTestLayerFiles();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_LAYERS));
        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS));
    }

    @Test
    public void resetCacheTest() throws IOException {
        saveTestCacheFiles();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_CACHE));
        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE));
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
        generalSettings.save(GeneralKeys.KEY_USERNAME, username);
        generalSettings.save(GeneralKeys.KEY_PASSWORD, password);

        assertEquals(username, generalSettings.getString(GeneralKeys.KEY_USERNAME));
        assertEquals(password, generalSettings.getString(GeneralKeys.KEY_PASSWORD));

        adminSettings.save(AdminKeys.KEY_VIEW_SENT, false);

        assertFalse(adminSettings.getBoolean(AdminKeys.KEY_VIEW_SENT));

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS)).exists() || new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS)).mkdir());
    }

    private void setupTestFormsDatabase() {
        ContentValues values = new ContentValues();
        values.put(FormsColumns.JRCACHE_FILE_PATH, storagePathProvider.getRelativeCachePath("3a76a386464925b6f3e53422673dfe3c.formdef"));
        values.put(FormsColumns.JR_FORM_ID, "jrFormId");
        values.put(FormsColumns.FORM_MEDIA_PATH, storagePathProvider.getRelativeFormPath("testFile1-media"));
        values.put(FormsColumns.DATE, "1487077903756");
        values.put(FormsColumns.DISPLAY_NAME, "displayName");
        values.put(FormsColumns.FORM_FILE_PATH, storagePathProvider.getRelativeFormPath("testFile1.xml"));
        Collect.getInstance().getContentResolver()
                .insert(FormsColumns.CONTENT_URI, values);

        assertEquals(1, getFormsCount());
    }

    private void setupTestInstancesDatabase() {
        new DatabaseInstancesRepository().save(new Instance.Builder()
                .instanceFilePath(storagePathProvider.getRelativeInstancePath("testDir1/testFile1"))
                .submissionUri("submissionUri")
                .displayName("formName")
                .jrFormId("jrformid")
                .jrVersion("jrversion")
                .build()
        );

        assertEquals(1, getInstancesCount());
    }

    private void createTestItemsetsDatabaseFile() throws IOException {
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA) + "/itemsets.db").createNewFile());
    }

    private void saveTestFormFiles() throws IOException {
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile1.xml").createNewFile());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile2.xml").createNewFile());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile3.xml").createNewFile());

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testDir1/testFile1-media").mkdirs());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testDir2/testFile2-media").mkdirs());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testDir3/testFile3-media/testFile.csv").mkdirs());
    }

    private void saveTestInstanceFiles() {
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES) + "/testDir1/testFile1.xml").mkdirs());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES) + "/testDir2/testFile2.xml").mkdirs());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES) + "/testDir3").mkdirs());
    }

    private void saveTestLayerFiles() throws IOException {
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile1").createNewFile());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile2").createNewFile());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile3").createNewFile());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile4").createNewFile());
    }

    private void saveTestCacheFiles() throws IOException {
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE) + "/testFile1").createNewFile());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE) + "/testFile2").createNewFile());
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE) + "/testFile3").createNewFile());
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
        return new DatabaseInstancesRepository().getAll().size();
    }

    private void assertFolderEmpty(String folder) {
        assertTrue(new File(folder).isDirectory());
        assertEquals(new File(folder).list().length, 0);
    }
}
