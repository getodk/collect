package org.odk.collect.android.utilities

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.Settings
import org.osmdroid.config.Configuration
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProjectResetterTest {
    private lateinit var projectResetter: ProjectResetter
    private lateinit var storagePathProvider: StoragePathProvider
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings
    private lateinit var formsRepositoryProvider: FormsRepositoryProvider
    private lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()

        val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>() as Application)
        projectResetter = component.projectResetter()
        storagePathProvider = component.storagePathProvider()
        generalSettings = component.settingsProvider().getGeneralSettings()
        adminSettings = component.settingsProvider().getAdminSettings()
        formsRepositoryProvider = component.formsRepositoryProvider()
        instancesRepositoryProvider = component.instancesRepositoryProvider()
    }

    @Test
    fun resetSettingsTest() {
        setupTestSettings()

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            generalSettings.getString(GeneralKeys.KEY_USERNAME),
            `is`(
                GeneralKeys.getDefaults()[GeneralKeys.KEY_USERNAME]
            )
        )
        assertThat(
            generalSettings.getString(GeneralKeys.KEY_PASSWORD),
            `is`(
                GeneralKeys.getDefaults()[GeneralKeys.KEY_PASSWORD]
            )
        )
        assertThat(
            adminSettings.getBoolean(AdminKeys.KEY_VIEW_SENT),
            `is`(
                AdminKeys.getDefaults()[AdminKeys.KEY_VIEW_SENT]
            )
        )
        assertEquals(0, formsRepositoryProvider.get().all.size)
        assertEquals(0, instancesRepositoryProvider.get().all.size)
    }

    @Test
    fun resetFormsTest() {
        saveTestFormFiles()
        setupTestFormsDatabase()
        createTestItemsetsDatabaseFile()

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_FORMS))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS))
        assertFalse(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA) + "/itemsets.db").exists())
    }

    @Test
    fun resetInstancesTest() {
        saveTestInstanceFiles()
        setupTestInstancesDatabase()

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_INSTANCES))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES))
    }

    @Test
    fun resetLayersTest() {
        saveTestLayerFiles()

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_LAYERS))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS))
    }

    @Test
    fun resetCacheTest() {
        saveTestCacheFiles()

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_CACHE))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE))
    }

    @Test
    fun resetOSMDroidTest() {
        saveTestOSMDroidFiles()

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_OSM_DROID))

        assertFolderEmpty(Configuration.getInstance().osmdroidTileCache.path)
    }

    private fun resetAppState(resetActions: List<Int>) {
        val failedResetActions = projectResetter.reset(resetActions)
        assertEquals(0, failedResetActions.size)
    }

    private fun setupTestSettings() {
        val username = "usernameTest"
        val password = "passwordTest"

        generalSettings.save(GeneralKeys.KEY_USERNAME, username)
        generalSettings.save(GeneralKeys.KEY_PASSWORD, password)

        assertEquals(username, generalSettings.getString(GeneralKeys.KEY_USERNAME))
        assertEquals(password, generalSettings.getString(GeneralKeys.KEY_PASSWORD))

        adminSettings.save(AdminKeys.KEY_VIEW_SENT, false)
        assertFalse(adminSettings.getBoolean(AdminKeys.KEY_VIEW_SENT))
        assertTrue(
            File(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS)).exists() || File(
                storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS)
            ).mkdir()
        )
    }

    private fun setupTestFormsDatabase() {
        FormsRepositoryProvider(ApplicationProvider.getApplicationContext()).get().save(
            Form.Builder()
                .formId("jrFormId")
                .displayName("displayName")
                .formFilePath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile1.xml")
                .build()
        )
        assertEquals(1, formsRepositoryProvider.get().all.size)
    }

    private fun setupTestInstancesDatabase() {
        InstancesRepositoryProvider(ApplicationProvider.getApplicationContext()).get().save(
            Instance.Builder()
                .instanceFilePath("testDir1/testFile1")
                .submissionUri("submissionUri")
                .displayName("formName")
                .formId("jrformid")
                .formVersion("jrversion")
                .build()
        )
        assertEquals(1, instancesRepositoryProvider.get().all.size)
    }

    private fun createTestItemsetsDatabaseFile() {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA) + "/itemsets.db").createNewFile())
    }

    private fun saveTestFormFiles() {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile1.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile2.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testFile3.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testDir1/testFile1-media").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testDir2/testFile2-media").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/testDir3/testFile3-media/testFile.csv").mkdirs())
    }

    private fun saveTestInstanceFiles() {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES) + "/testDir1/testFile1.xml").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES) + "/testDir2/testFile2.xml").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES) + "/testDir3").mkdirs())
    }

    private fun saveTestLayerFiles() {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile1").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile2").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile3").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS) + "/testFile4").createNewFile())
    }

    private fun saveTestCacheFiles() {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE) + "/testFile1").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE) + "/testFile2").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE) + "/testFile3").createNewFile())
    }

    private fun saveTestOSMDroidFiles() {
        assertTrue(File(Configuration.getInstance().osmdroidTileCache.path + "/testFile1").mkdirs())
        assertTrue(File(Configuration.getInstance().osmdroidTileCache.path + "/testFile2").mkdirs())
        assertTrue(File(Configuration.getInstance().osmdroidTileCache.path + "/testFile3").mkdirs())
    }

    private fun assertFolderEmpty(folder: String) {
        assertTrue(File(folder).isDirectory)
        assertEquals(File(folder).list().size, 0)
    }
}
