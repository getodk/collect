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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.events.RxEventBus
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.logic.PropertyManager
import org.odk.collect.android.permissions.PermissionsProvider
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.projects.Project
import org.odk.collect.shared.Settings
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProjectResetterTest {
    private lateinit var projectResetter: ProjectResetter
    private lateinit var storagePathProvider: StoragePathProvider
    private lateinit var settingsProvider: SettingsProvider
    private lateinit var formsRepositoryProvider: FormsRepositoryProvider
    private lateinit var instancesRepositoryProvider: InstancesRepositoryProvider
    private lateinit var currentProjectId: String
    private lateinit var anotherProjectId: String

    private val propertyManager = mock<PropertyManager>()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesPropertyManager(
                eventBus: RxEventBus?,
                permissionsProvider: PermissionsProvider?,
                deviceDetailsProvider: DeviceDetailsProvider?,
                settingsProvider: SettingsProvider?
            ): PropertyManager {
                return propertyManager
            }
        })

        currentProjectId = CollectHelpers.setupDemoProject()
        anotherProjectId = CollectHelpers.createProject(Project.New("Another project", "A", "#cccccc"))

        val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>() as Application)
        projectResetter = component.projectResetter()
        storagePathProvider = component.storagePathProvider()
        settingsProvider = component.settingsProvider()
        formsRepositoryProvider = component.formsRepositoryProvider()
        instancesRepositoryProvider = component.instancesRepositoryProvider()
    }

    @Test
    fun `Reset settings clears general settings for current project`() {
        setupTestGeneralSettings(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getGeneralSettings(currentProjectId).getString(ProjectKeys.KEY_USERNAME),
            `is`(
                ProjectKeys.defaults[ProjectKeys.KEY_USERNAME]
            )
        )
        assertThat(
            getGeneralSettings(currentProjectId).getString(ProjectKeys.KEY_PASSWORD),
            `is`(
                ProjectKeys.defaults[ProjectKeys.KEY_PASSWORD]
            )
        )
    }

    @Test
    fun `Reset settings does not clear general settings for another projects`() {
        setupTestGeneralSettings(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getGeneralSettings(anotherProjectId).getString(ProjectKeys.KEY_USERNAME),
            `is`(
                "usernameTest"
            )
        )
        assertThat(
            getGeneralSettings(anotherProjectId).getString(ProjectKeys.KEY_PASSWORD),
            `is`(
                "passwordTest"
            )
        )
    }

    @Test
    fun `Reset settings clears admin settings for current project`() {
        setupTestAdminSettings(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getAdminSettings(currentProjectId).getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT),
            `is`(
                ProtectedProjectKeys.defaults[ProtectedProjectKeys.KEY_VIEW_SENT]
            )
        )
    }

    @Test
    fun `Reset settings does not clear admin settings for another projects`() {
        setupTestAdminSettings(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getAdminSettings(anotherProjectId).getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT),
            `is`(
                false
            )
        )
    }

    @Test
    fun `Reset settings clears settings folder for current project`() {
        setupTestSettingsFolder(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS, currentProjectId))
    }

    @Test
    fun `Reset settings does not clear settings folder for another projects`() {
        setupTestSettingsFolder(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS, anotherProjectId), "settings.png").exists())
    }

    @Test
    fun `Reset settings reloads property manager`() {
        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        verify(propertyManager).reload()
    }

    @Test
    fun `Reset forms clears forms for current project`() {
        saveTestFormFiles(currentProjectId)
        setupTestFormsDatabase(currentProjectId)
        createTestItemsetsDatabaseFile(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_FORMS))

        assertEquals(0, formsRepositoryProvider.get(currentProjectId).all.size)
        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, currentProjectId))
        assertFalse(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, currentProjectId) + "/itemsets.db").exists())
    }

    @Test
    fun `Reset forms does not clear forms for another projects`() {
        saveTestFormFiles(anotherProjectId)
        setupTestFormsDatabase(anotherProjectId)
        createTestItemsetsDatabaseFile(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_FORMS))

        assertEquals(1, formsRepositoryProvider.get(anotherProjectId).all.size)
        assertTestFormFiles(anotherProjectId)
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, anotherProjectId) + "/itemsets.db").exists())
    }

    @Test
    fun `Reset instances clears instances for current project`() {
        saveTestInstanceFiles(currentProjectId)
        setupTestInstancesDatabase(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_INSTANCES))

        assertEquals(0, instancesRepositoryProvider.get(currentProjectId).all.size)
        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, currentProjectId))
    }

    @Test
    fun `Reset instances does not clear instances for another projects`() {
        saveTestInstanceFiles(anotherProjectId)
        setupTestInstancesDatabase(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_INSTANCES))

        assertEquals(1, instancesRepositoryProvider.get(anotherProjectId).all.size)
        assertTestInstanceFiles(anotherProjectId)
    }

    @Test
    fun `Reset layers clears layers for current project`() {
        saveTestLayerFiles(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_LAYERS))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, currentProjectId))
    }

    @Test
    fun `Reset layers does not clear layers for another projects`() {
        saveTestLayerFiles(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_LAYERS))

        assertTestLayerFiles(anotherProjectId)
    }

    @Test
    fun `Reset cache clears cache for project`() {
        saveTestCacheFiles(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_CACHE))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, currentProjectId))
    }

    @Test
    fun `Reset cache does not clear cache for another projects`() {
        saveTestCacheFiles(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_CACHE))

        assertTestCacheFiles(anotherProjectId)
    }

    private fun resetAppState(resetActions: List<Int>) {
        val failedResetActions = projectResetter.reset(resetActions)
        assertEquals(0, failedResetActions.size)
    }

    private fun setupTestGeneralSettings(uuid: String) {
        getGeneralSettings(uuid).save(ProjectKeys.KEY_USERNAME, "usernameTest")
        getGeneralSettings(uuid).save(ProjectKeys.KEY_PASSWORD, "passwordTest")
    }

    private fun setupTestAdminSettings(uuid: String) {
        getAdminSettings(uuid).save(ProtectedProjectKeys.KEY_VIEW_SENT, false)
    }

    private fun setupTestSettingsFolder(uuid: String) {
        assertTrue(
            File(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS, uuid)).exists() || File(
                storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS, uuid)
            ).mkdir()
        )

        File(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS, uuid), "settings.png").createNewFile()
    }

    private fun setupTestFormsDatabase(uuid: String) {
        FormsRepositoryProvider(ApplicationProvider.getApplicationContext()).get(uuid).save(
            Form.Builder()
                .formId("jrFormId")
                .displayName("displayName")
                .formFilePath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testFile1.xml")
                .build()
        )
        assertEquals(1, formsRepositoryProvider.get(uuid).all.size)
    }

    private fun setupTestInstancesDatabase(uuid: String) {
        InstancesRepositoryProvider(ApplicationProvider.getApplicationContext()).get(uuid).save(
            Instance.Builder()
                .instanceFilePath("testDir1/testFile1")
                .submissionUri("submissionUri")
                .displayName("formName")
                .formId("jrformid")
                .formVersion("jrversion")
                .build()
        )
        assertEquals(1, instancesRepositoryProvider.get(uuid).all.size)
    }

    private fun createTestItemsetsDatabaseFile(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, uuid) + "/itemsets.db").createNewFile())
    }

    private fun saveTestFormFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testFile1.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testFile2.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testFile3.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testDir1/testFile1-media").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testDir2/testFile2-media").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testDir3/testFile3-media/testFile.csv").mkdirs())
    }

    private fun assertTestFormFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testFile1.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testFile2.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testFile3.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testDir1/testFile1-media").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testDir2/testFile2-media").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, uuid) + "/testDir3/testFile3-media/testFile.csv").exists())
    }

    private fun saveTestInstanceFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, uuid) + "/testDir1/testFile1.xml").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, uuid) + "/testDir2/testFile2.xml").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, uuid) + "/testDir3").mkdirs())
    }

    private fun assertTestInstanceFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, uuid) + "/testDir1/testFile1.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, uuid) + "/testDir2/testFile2.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, uuid) + "/testDir3").exists())
    }

    private fun saveTestLayerFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile1").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile2").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile3").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile4").createNewFile())
    }

    private fun assertTestLayerFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile1").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile2").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile3").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, uuid) + "/testFile4").exists())
    }

    private fun saveTestCacheFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, uuid) + "/testFile1").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, uuid) + "/testFile2").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, uuid) + "/testFile3").createNewFile())
    }

    private fun assertTestCacheFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, uuid) + "/testFile1").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, uuid) + "/testFile2").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, uuid) + "/testFile3").exists())
    }

    private fun assertFolderEmpty(folder: String) {
        assertTrue(File(folder).isDirectory)
        assertTrue(File(folder).list().isEmpty())
    }

    fun getGeneralSettings(uuid: String): Settings {
        return settingsProvider.getGeneralSettings(uuid)
    }

    fun getAdminSettings(uuid: String): Settings {
        return settingsProvider.getAdminSettings(uuid)
    }
}
