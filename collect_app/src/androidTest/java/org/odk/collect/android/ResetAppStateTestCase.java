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

package org.odk.collect.android;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ResetUtility;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResetAppStateTestCase {

    @Before
    public void setUp() throws IOException {
        resetAppState(Arrays.asList(
                ResetUtility.ResetAction.RESET_PREFERENCES, ResetUtility.ResetAction.RESET_INSTANCES,
                ResetUtility.ResetAction.RESET_FORMS, ResetUtility.ResetAction.RESET_LAYERS,
                ResetUtility.ResetAction.RESET_CACHE, ResetUtility.ResetAction.RESET_OSM_DROID
        ));
    }

    @After
    public void tearDown() throws IOException {
        resetAppState(Arrays.asList(
                ResetUtility.ResetAction.RESET_PREFERENCES, ResetUtility.ResetAction.RESET_INSTANCES,
                ResetUtility.ResetAction.RESET_FORMS, ResetUtility.ResetAction.RESET_LAYERS,
                ResetUtility.ResetAction.RESET_CACHE, ResetUtility.ResetAction.RESET_OSM_DROID
        ));
    }

    @Test
    public void resetSettingsTest() throws IOException {
        setupTestSettings();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_PREFERENCES));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        assertEquals(settings.getString(PreferenceKeys.KEY_USERNAME, ""), "");
        assertEquals(settings.getString(PreferenceKeys.KEY_PASSWORD, ""), "");
        assertTrue(settings.getBoolean(AdminKeys.KEY_VIEW_SENT, true));

        assertEquals(0, getFormsCount());
        assertEquals(0, getInstancesCount());
    }

    @Test
    public void resetFormsTest() throws IOException {
        saveTestFormFiles();
        setupTestFormsDatabase();
        createTestItemsetsDatabaseFile();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_FORMS));
        assertFolderEmpty(Collect.FORMS_PATH);
        assertFalse(new File(Collect.METADATA_PATH + "/itemsets.db").exists());
    }

    @Test
    public void resetInstancesTest() throws IOException {
        saveTestInstanceFiles();
        setupTestInstancesDatabase();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_INSTANCES));
        assertFolderEmpty(Collect.INSTANCES_PATH);
    }

    @Test
    public void resetLayersTest() throws IOException {
        saveTestLayerFiles();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_LAYERS));
        assertFolderEmpty(Collect.OFFLINE_LAYERS);
    }

    @Test
    public void resetCacheTest() throws IOException {
        saveTestCacheFiles();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_CACHE));
        assertFolderEmpty(Collect.CACHE_PATH);
    }

    @Test
    public void resetOSMDroidTest() throws IOException {
        saveTestOSMDroidFiles();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_OSM_DROID));
        assertFolderEmpty(Configuration.getInstance().getOsmdroidTileCache().getPath());
    }

    private void resetAppState(List<Integer> resetActions) {
        List<Integer> failedResetActions = new ResetUtility().reset(InstrumentationRegistry.getTargetContext(), resetActions);
        assertEquals(0, failedResetActions.size());
    }

    private void setupTestSettings() throws IOException {
        String username = "usernameTest";
        String password = "passwordTest";
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        settings
                .edit()
                .putString(PreferenceKeys.KEY_USERNAME, username)
                .putString(PreferenceKeys.KEY_PASSWORD, password)
                .apply();

        assertEquals(username, settings.getString(PreferenceKeys.KEY_USERNAME, null));
        assertEquals(password, settings.getString(PreferenceKeys.KEY_PASSWORD, null));

        settings
                .edit()
                .putBoolean(AdminKeys.KEY_VIEW_SENT, false)
                .apply();

        assertFalse(settings.getBoolean(AdminKeys.KEY_VIEW_SENT, false));

        assertTrue(new File(Collect.SETTINGS).exists() || new File(Collect.SETTINGS).mkdir());
        assertTrue(new File(Collect.SETTINGS + "/collect.settings").createNewFile());
        assertTrue(new File(Collect.ODK_ROOT + "/collect.settings").createNewFile());
    }

    private void setupTestFormsDatabase() {
        ContentValues values = new ContentValues();
        values.put(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH, Collect.ODK_ROOT + "/.cache/3a76a386464925b6f3e53422673dfe3c.formdef");
        values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, "jrFormId");
        values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, Collect.FORMS_PATH + "/testFile1-media");
        values.put(FormsProviderAPI.FormsColumns.DATE, "1487077903756");
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, "displayName");
        values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, Collect.FORMS_PATH + "/testFile1.xml");
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT, "Added on Tue, Feb 14, 2017 at 14:21");
        Collect.getInstance().getContentResolver()
                .insert(FormsProviderAPI.FormsColumns.CONTENT_URI, values);

        assertEquals(1, getFormsCount());
    }

    private void setupTestInstancesDatabase() {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, Collect.INSTANCES_PATH + "/testDir1/testFile1");
        values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, "submissionUri");
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, "displayName");
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, "formName");
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, "jrformid");
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, "jrversion");
        Collect.getInstance().getContentResolver()
                .insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);

        assertEquals(1, getInstancesCount());
    }

    private void createTestItemsetsDatabaseFile() throws IOException {
        assertTrue(new File(Collect.METADATA_PATH + "/itemsets.db").createNewFile());
    }

    private void saveTestFormFiles() throws IOException {
        assertTrue(new File(Collect.FORMS_PATH + "/testFile1.xml").createNewFile());
        assertTrue(new File(Collect.FORMS_PATH + "/testFile2.xml").createNewFile());
        assertTrue(new File(Collect.FORMS_PATH + "/testFile3.xml").createNewFile());

        assertTrue(new File(Collect.FORMS_PATH + "/testDir1/testFile1-media").mkdirs());
        assertTrue(new File(Collect.FORMS_PATH + "/testDir2/testFile2-media").mkdirs());
        assertTrue(new File(Collect.FORMS_PATH + "/testDir3/testFile3-media/testFile.csv").mkdirs());
    }

    private void saveTestInstanceFiles() {
        assertTrue(new File(Collect.INSTANCES_PATH + "/testDir1/testFile1.xml").mkdirs());
        assertTrue(new File(Collect.INSTANCES_PATH + "/testDir2/testFile2.xml").mkdirs());
        assertTrue(new File(Collect.INSTANCES_PATH + "/testDir3").mkdirs());
    }

    private void saveTestLayerFiles() throws IOException {
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile1").createNewFile());
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile2").createNewFile());
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile3").createNewFile());
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile4").createNewFile());
    }

    private void saveTestCacheFiles() throws IOException {
        assertTrue(new File(Collect.CACHE_PATH + "/testFile1").createNewFile());
        assertTrue(new File(Collect.CACHE_PATH + "/testFile2").createNewFile());
        assertTrue(new File(Collect.CACHE_PATH + "/testFile3").createNewFile());
    }

    private void saveTestOSMDroidFiles() throws IOException {
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile1").mkdirs());
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile2").mkdirs());
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile3").mkdirs());
    }

    private int getFormsCount() {
        int forms = 0;
        Cursor cursor = Collect.getInstance().getContentResolver().query(
                FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null,
                FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC");
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
                InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null, null,
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
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
