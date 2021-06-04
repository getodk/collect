package org.odk.collect.android.utilities;

import android.app.Application;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.configure.ServerRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.shared.Settings;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class ApplicationResetterTest {

    private final ServerRepository serverRepository = mock(ServerRepository.class);
    private StoragePathProvider storagePathProvider;
    private Settings generalSettings;
    private Settings adminSettings;

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ServerRepository providesServerRepository(Context context, SettingsProvider settingsProvider) {
                return serverRepository;
            }
        });

        CollectHelpers.setupDemoProject();

        AppDependencyComponent component = DaggerUtils.getComponent((Application) ApplicationProvider.getApplicationContext());
        storagePathProvider = component.storagePathProvider();
        generalSettings = component.settingsProvider().getGeneralSettings();
        adminSettings = component.settingsProvider().getAdminSettings();
    }

    @Test
    public void resetSettingsTest() throws IOException {
        setupTestSettings();
        resetAppState(Collections.singletonList(ApplicationResetter.ResetAction.RESET_PREFERENCES));

        assertThat(generalSettings.getString(GeneralKeys.KEY_USERNAME), is(GeneralKeys.getDefaults().get(GeneralKeys.KEY_USERNAME)));
        assertThat(generalSettings.getString(GeneralKeys.KEY_PASSWORD), is(GeneralKeys.getDefaults().get(GeneralKeys.KEY_PASSWORD)));
        assertThat(adminSettings.getBoolean(AdminKeys.KEY_VIEW_SENT), is(AdminKeys.getDefaults().get(AdminKeys.KEY_VIEW_SENT)));

        assertEquals(0, getFormsCount());
        assertEquals(0, getInstancesCount());
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
        new FormsRepositoryProvider(ApplicationProvider.getApplicationContext()).get().save(new Form.Builder()
                .formId("jrFormId")
                .displayName("displayName")
                .formFilePath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile1.xml")
                .build()
        );

        assertEquals(1, getFormsCount());
    }

    private void setupTestInstancesDatabase() {
        new InstancesRepositoryProvider(ApplicationProvider.getApplicationContext()).get().save(new Instance.Builder()
                .instanceFilePath("testDir1/testFile1")
                .submissionUri("submissionUri")
                .displayName("formName")
                .formId("jrformid")
                .formVersion("jrversion")
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
        return new FormsRepositoryProvider(ApplicationProvider.getApplicationContext()).get().getAll().size();
    }

    private int getInstancesCount() {
        return new InstancesRepositoryProvider(ApplicationProvider.getApplicationContext()).get().getAll().size();
    }

    private void assertFolderEmpty(String folder) {
        assertTrue(new File(folder).isDirectory());
        assertEquals(new File(folder).list().length, 0);
    }

    @Test
    public void resettingPreferences_alsoResetsServers() {
        ApplicationResetter applicationResetter = new ApplicationResetter();
        applicationResetter.reset(asList(ApplicationResetter.ResetAction.RESET_PREFERENCES));
        verify(serverRepository).clear();
    }
}
