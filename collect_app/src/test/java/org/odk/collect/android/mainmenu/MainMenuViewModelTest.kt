package org.odk.collect.android.mainmenu

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.projects.Project
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class MainMenuViewModelTest {
    private val formsRepository = InMemFormsRepository()
    private val formsRepositoryProvider = mock<FormsRepositoryProvider>().apply {
        whenever(get()).thenReturn(formsRepository)
    }
    private val instancesRepository = InMemInstancesRepository()
    private val instancesRepositoryProvider = mock<InstancesRepositoryProvider>().apply {
        whenever(get()).thenReturn(instancesRepository)
    }
    private val autoSendSettingsProvider = mock<AutoSendSettingsProvider>()
    private val settingsProvider = InMemSettingsProvider()

    @Test
    fun `version when beta release returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `version when dirty beta release returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-dirty")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `version when beta tag returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `versionCommitDescription when release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7")
        assertThat(viewModel.versionCommitDescription, equalTo(null))
    }

    @Test
    fun `versionCommitDescription when dirty release returns dirty`() {
        val viewModel = createViewModelWithVersion("v1.1.7-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("dirty"))
    }

    @Test
    fun `versionCommitDescription when beta release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7-beta.7")
        assertThat(viewModel.versionCommitDescription, equalTo(null))
    }

    @Test
    fun `versionCommitDescription when dirty beta release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7-beta.7-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("dirty"))
    }

    @Test
    fun `versionCommitDescription when beta tag returns commit count and SHA`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4"))
    }

    @Test
    fun `versionCommitDescription when release tag returns commit count and SHA`() {
        val viewModel = createViewModelWithVersion("v1.23.0-181-ge51d004d4")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4"))
    }

    @Test
    fun `versionCommitDescription when dirty commit returns commit count and SHA and dirty tag`() {
        val viewModel = createViewModelWithVersion("v1.14.0-181-ge51d004d4-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4-dirty"))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is saved as draft, editing drafts is enabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, true)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .canEditWhenComplete(true)
                .build()
        )

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarType = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarType.first, equalTo(R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.second, equalTo(R.string.edit_form))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is saved as draft, editing drafts is enabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, true)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .canEditWhenComplete(false)
                .build()
        )

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarType = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarType.first, equalTo(R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.second, equalTo(R.string.edit_form))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is saved as draft, editing drafts is disabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, false)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .canEditWhenComplete(true)
                .build()
        )

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarType = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarType.first, equalTo(R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.second, equalTo(R.string.view_form))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is saved as draft, editing drafts is disabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, false)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .canEditWhenComplete(false)
                .build()
        )

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarType = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarType.first, equalTo(R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.second, equalTo(R.string.view_form))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is finalized, auto send is disabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .canEditWhenComplete(true)
                .build()
        )
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(false)

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarDetails = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarDetails.first, equalTo(R.string.form_saved))
        assertThat(formSavedSnackbarDetails.second, equalTo(R.string.view_form))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is finalized, auto send is disabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .canEditWhenComplete(false)
                .build()
        )
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(false)

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarDetails = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarDetails.first, equalTo(R.string.form_saved))
        assertThat(formSavedSnackbarDetails.second, equalTo(null))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is finalized, auto send is enabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .canEditWhenComplete(true)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarDetails = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarDetails.first, equalTo(R.string.form_sending))
        assertThat(formSavedSnackbarDetails.second, equalTo(R.string.view_form))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return proper message and action when the corresponding instance is finalized, auto send is enabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .canEditWhenComplete(false)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarDetails = viewModel.getFormSavedSnackbarDetails(uri)!!
        assertThat(formSavedSnackbarDetails.first, equalTo(R.string.form_sending))
        assertThat(formSavedSnackbarDetails.second, equalTo(null))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return null when the corresponding instance is already sent`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarDetails = viewModel.getFormSavedSnackbarDetails(uri)
        assertThat(formSavedSnackbarDetails, equalTo(null))
    }

    @Test
    fun `getFormSavedSnackbarDetails should return null when the corresponding instance failed to sent`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        val formSavedSnackbarDetails = viewModel.getFormSavedSnackbarDetails(uri)
        assertThat(formSavedSnackbarDetails, equalTo(null))
    }

    private fun createViewModelWithVersion(version: String): MainMenuViewModel {
        return MainMenuViewModel(mock(), VersionInformation { version }, settingsProvider, mock(), mock(), formsRepositoryProvider, instancesRepositoryProvider, autoSendSettingsProvider)
    }
}
