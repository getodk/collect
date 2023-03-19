package org.odk.collect.android.formmanagement.formmap

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceUtils
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shared.TempFiles
import org.odk.collect.testshared.FakeScheduler
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class FormMapViewModelTest {

    private val formsRepository = InMemFormsRepository()
    private val instancesRepository = InMemInstancesRepository()
    private val settingsProvider = InMemSettingsProvider().also {
        it.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, true)
    }

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val scheduler = FakeScheduler()

    @Test
    fun `returns count based on form instances`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .build()
        )

        val viewModel = createAndLoadViewModel(form)
        assertThat(viewModel.getItemCount().value, equalTo(2))
    }

    @Test
    fun `returns items based on form instances with point geometry`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )

        instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("")
                .geometryType("Something else")
                .build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .build()
        )

        val instanceWithPoint = instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .build()
        )

        val viewModel = createAndLoadViewModel(form)
        assertThat(viewModel.getMappableItems().value!!.size, equalTo(1))

        val expectedItem = MappableSelectItem.WithAction(
            instanceWithPoint.dbId,
            2.0,
            1.0,
            R.drawable.ic_room_form_state_incomplete_24dp,
            R.drawable.ic_room_form_state_incomplete_48dp,
            instanceWithPoint.displayName,
            listOf(
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_saved,
                    formatDate(
                        R.string.saved_on_date_at_time,
                        instanceWithPoint.lastStatusChangeDate
                    )
                )
            ),
            action = MappableSelectItem.IconifiedText(
                R.drawable.ic_edit,
                application.getString(R.string.review_data)
            )
        )
        assertThat(viewModel.getMappableItems().value!![0], equalTo(expectedItem))
    }

    @Test
    fun `finalized instances with geometry have edit action and no info`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        val viewModel = createAndLoadViewModel(form)
        val expectedItem = MappableSelectItem.WithAction(
            instance.dbId,
            2.0,
            1.0,
            R.drawable.ic_room_form_state_complete_24dp,
            R.drawable.ic_room_form_state_complete_48dp,
            instance.displayName,
            listOf(
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_finalized,
                    formatDate(
                        R.string.finalized_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                )
            ),
            action = MappableSelectItem.IconifiedText(
                R.drawable.ic_edit,
                application.getString(R.string.review_data)
            )
        )
        assertThat(viewModel.getMappableItems().value!![0], equalTo(expectedItem))
    }

    @Test
    fun `finalized instances with geometry when edit after save disabled have view action and no info`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, false)

        val viewModel = createAndLoadViewModel(form)
        val expectedItem = MappableSelectItem.WithAction(
            instance.dbId,
            2.0,
            1.0,
            R.drawable.ic_room_form_state_complete_24dp,
            R.drawable.ic_room_form_state_complete_48dp,
            instance.displayName,
            listOf(
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_finalized,
                    formatDate(
                        R.string.finalized_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                )
            ),
            action = MappableSelectItem.IconifiedText(
                R.drawable.ic_visibility,
                application.getString(R.string.view_data)
            )
        )
        assertThat(viewModel.getMappableItems().value!![0], equalTo(expectedItem))
    }

    @Test
    fun `deleted instances with geometry have info and no action`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .deletedDate(123L)
                .build()
        )

        val viewModel = createAndLoadViewModel(form)
        val expectedItem = MappableSelectItem.WithInfo(
            instance.dbId,
            2.0,
            1.0,
            R.drawable.ic_room_form_state_incomplete_24dp,
            R.drawable.ic_room_form_state_incomplete_48dp,
            instance.displayName,
            listOf(
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_saved,
                    formatDate(
                        R.string.saved_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                )
            ),
            info = formatDate(R.string.deleted_on_date_at_time, 123L),
        )
        assertThat(viewModel.getMappableItems().value!![0], equalTo(expectedItem))
    }

    @Test
    fun `submitted instances with geometry have view action and no info`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val viewModel = createAndLoadViewModel(form)
        val expectedItem = MappableSelectItem.WithAction(
            instance.dbId,
            2.0,
            1.0,
            R.drawable.ic_room_form_state_submitted_24dp,
            R.drawable.ic_room_form_state_submitted_48dp,
            instance.displayName,
            listOf(
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_submited,
                    formatDate(
                        R.string.sent_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                )
            ),
            action = MappableSelectItem.IconifiedText(
                R.drawable.ic_visibility,
                application.getString(R.string.view_data)
            )
        )
        assertThat(viewModel.getMappableItems().value!![0], equalTo(expectedItem))
    }

    @Test
    fun `instances that failed to submit with geometry have view action and no info`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        val viewModel = createAndLoadViewModel(form)
        val expectedItem = MappableSelectItem.WithAction(
            instance.dbId,
            2.0,
            1.0,
            R.drawable.ic_room_form_state_submission_failed_24dp,
            R.drawable.ic_room_form_state_submission_failed_48dp,
            instance.displayName,
            listOf(
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_submission_failed,
                    formatDate(
                        R.string.sending_failed_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                )
            ),
            action = MappableSelectItem.IconifiedText(
                R.drawable.ic_visibility,
                application.getString(R.string.view_data)
            )
        )
        assertThat(viewModel.getMappableItems().value!![0], equalTo(expectedItem))
    }

    @Test
    fun `instances that cannot be edited after completion with geometry have info and no action`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(false)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(false)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance(
                form.formId,
                form.version,
                TempFiles.createTempDir().absolutePath
            )
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(false)
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        val viewModel = createAndLoadViewModel(form)
        val items = viewModel.getMappableItems().value

        assertThat(
            (items!![0] as MappableSelectItem.WithInfo).info,
            equalTo(application.getString(R.string.cannot_edit_completed_form))
        )

        assertThat(
            (items[1] as MappableSelectItem.WithInfo).info,
            equalTo(application.getString(R.string.cannot_edit_completed_form))
        )

        assertThat(
            (items[2] as MappableSelectItem.WithInfo).info,
            equalTo(application.getString(R.string.cannot_edit_completed_form))
        )
    }

    @Test
    fun `is loading is true and items is null while forms and instances are being fetched`() {
        val form = formsRepository.save(
            FormUtils.buildForm("id", "version", TempFiles.createTempDir().absolutePath)
                .build()
        )

        val viewModel = createViewModel(form)
        assertThat(viewModel.isLoading().value, equalTo(false))
        assertThat(viewModel.getMappableItems().value, equalTo(null))

        viewModel.load()
        assertThat(viewModel.isLoading().value, equalTo(true))
        assertThat(viewModel.getMappableItems().value, equalTo(null))

        scheduler.runBackground()
        assertThat(viewModel.isLoading().value, equalTo(false))
    }

    private fun createAndLoadViewModel(form: Form): FormMapViewModel {
        val viewModel = createViewModel(form)
        viewModel.load()
        scheduler.runBackground()
        return viewModel
    }

    private fun createViewModel(form: Form): FormMapViewModel {
        return FormMapViewModel(
            application.resources,
            form.dbId,
            formsRepository,
            instancesRepository,
            settingsProvider,
            scheduler
        )
    }

    private fun formatDate(string: Int, date: Long): String {
        return SimpleDateFormat(
            application.getString(string),
            Locale.getDefault()
        ).format(date)
    }
}
