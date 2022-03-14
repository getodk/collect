package org.odk.collect.android.activities

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils.buildForm
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceUtils.buildInstance
import org.odk.collect.geo.MappableSelectItem
import org.odk.collect.geo.SelectionMapViewModel
import org.odk.collect.shared.TempFiles.createTempDir
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class FormMapActivityTest {

    private val formsRepository = InMemFormsRepository()
    private val instancesRepository = InMemInstancesRepository()
    private val application = getApplicationContext<Application>()

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()

        val formsRepositoryProvider = mock<FormsRepositoryProvider> {
            on { get() } doReturn formsRepository
        }

        val instancesRepositoryProvider = mock<InstancesRepositoryProvider> {
            on { get() } doReturn instancesRepository
        }

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesFormsRepositoryProvider(application: Application?): FormsRepositoryProvider {
                return formsRepositoryProvider
            }

            override fun providesInstancesRepositoryProvider(
                context: Context?,
                storagePathProvider: StoragePathProvider?
            ): InstancesRepositoryProvider {
                return instancesRepositoryProvider
            }
        })
    }

    @Test
    fun `sets item count based on form instances`() {
        val form = formsRepository.save(
            buildForm("id", "version", createTempDir().absolutePath)
                .build()
        )
        instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .build()
        )
        instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .build()
        )

        val scenario = launchActivity(form)
        scenario.onActivity {
            val viewModel = ViewModelProvider(it)[SelectionMapViewModel::class.java]
            assertThat(viewModel.getItemCount().value, equalTo(2))
        }
    }

    @Test
    fun `sets items based on form instances with point geometry`() {
        val form = formsRepository.save(
            buildForm("id", "version", createTempDir().absolutePath)
                .build()
        )

        instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("")
                .geometryType("Something else")
                .build()
        )
        instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .build()
        )

        val instanceWithPoint = instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .build()
        )

        val scenario = launchActivity(form)
        scenario.onActivity {
            val viewModel = ViewModelProvider(it)[SelectionMapViewModel::class.java]
            assertThat(viewModel.getMappableItems().value.size, equalTo(1))

            val expectedItem = MappableSelectItem(
                instanceWithPoint.dbId,
                2.0,
                1.0,
                R.drawable.ic_room_form_state_incomplete_24dp,
                R.drawable.ic_room_form_state_incomplete_48dp,
                instanceWithPoint.displayName,
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_saved,
                    formatDate(
                        R.string.saved_on_date_at_time,
                        instanceWithPoint.lastStatusChangeDate
                    )
                ),
                info = null,
                action = MappableSelectItem.IconifiedText(
                    R.drawable.ic_edit,
                    application.getString(R.string.review_data)
                )
            )
            assertThat(viewModel.getMappableItems().value[0], equalTo(expectedItem))
        }
    }

    @Test // Replace with a unit test
    fun `finalized instances with geometry have edit action and no info`() {
        val form = formsRepository.save(
            buildForm("id", "version", createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        val scenario = launchActivity(form)
        scenario.onActivity {
            val viewModel = ViewModelProvider(it)[SelectionMapViewModel::class.java]
            val expectedItem = MappableSelectItem(
                instance.dbId,
                2.0,
                1.0,
                R.drawable.ic_room_form_state_complete_24dp,
                R.drawable.ic_room_form_state_complete_48dp,
                instance.displayName,
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_finalized,
                    formatDate(
                        R.string.finalized_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                ),
                info = null,
                action = MappableSelectItem.IconifiedText(
                    R.drawable.ic_edit,
                    application.getString(R.string.review_data)
                )
            )
            assertThat(viewModel.getMappableItems().value[0], equalTo(expectedItem))
        }
    }

    @Test // Replace with a unit test
    fun `deleted instances with geometry have info and no action`() {
        val form = formsRepository.save(
            buildForm("id", "version", createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .deletedDate(123L)
                .build()
        )

        val scenario = launchActivity(form)
        scenario.onActivity {
            val viewModel = ViewModelProvider(it)[SelectionMapViewModel::class.java]
            val expectedItem = MappableSelectItem(
                instance.dbId,
                2.0,
                1.0,
                R.drawable.ic_room_form_state_incomplete_24dp,
                R.drawable.ic_room_form_state_incomplete_48dp,
                instance.displayName,
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_saved,
                    formatDate(
                        R.string.saved_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                ),
                info = formatDate(R.string.deleted_on_date_at_time, 123L),
                action = null
            )
            assertThat(viewModel.getMappableItems().value[0], equalTo(expectedItem))
        }
    }

    @Test // Replace with a unit test
    fun `submitted instances with geometry have view action and no info`() {
        val form = formsRepository.save(
            buildForm("id", "version", createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val scenario = launchActivity(form)
        scenario.onActivity {
            val viewModel = ViewModelProvider(it)[SelectionMapViewModel::class.java]
            val expectedItem = MappableSelectItem(
                instance.dbId,
                2.0,
                1.0,
                R.drawable.ic_room_form_state_submitted_24dp,
                R.drawable.ic_room_form_state_submitted_48dp,
                instance.displayName,
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_submited,
                    formatDate(
                        R.string.sent_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                ),
                info = null,
                action = MappableSelectItem.IconifiedText(
                    R.drawable.ic_visibility,
                    application.getString(R.string.view_data)
                )
            )
            assertThat(viewModel.getMappableItems().value[0], equalTo(expectedItem))
        }
    }

    @Test // Replace with a unit test
    fun `instances that failed to submit with geometry have view action and no info`() {
        val form = formsRepository.save(
            buildForm("id", "version", createTempDir().absolutePath)
                .build()
        )
        val instance = instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        val scenario = launchActivity(form)
        scenario.onActivity {
            val viewModel = ViewModelProvider(it)[SelectionMapViewModel::class.java]
            val expectedItem = MappableSelectItem(
                instance.dbId,
                2.0,
                1.0,
                R.drawable.ic_room_form_state_submission_failed_24dp,
                R.drawable.ic_room_form_state_submission_failed_48dp,
                instance.displayName,
                MappableSelectItem.IconifiedText(
                    R.drawable.form_state_submission_failed,
                    formatDate(
                        R.string.sending_failed_on_date_at_time,
                        instance.lastStatusChangeDate
                    )
                ),
                info = null,
                action = MappableSelectItem.IconifiedText(
                    R.drawable.ic_visibility,
                    application.getString(R.string.view_data)
                )
            )
            assertThat(viewModel.getMappableItems().value[0], equalTo(expectedItem))
        }
    }

    @Test // Replace with a unit test
    fun `instances that cannot be edited after completion with geometry have info and no action`() {
        val form = formsRepository.save(
            buildForm("id", "version", createTempDir().absolutePath)
                .build()
        )
        instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(false)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )
        instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(false)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )
        instancesRepository.save(
            buildInstance(form.formId, form.version, createTempDir().absolutePath)
                .geometry("{ \"coordinates\": [1.0, 2.0] }")
                .geometryType("Point")
                .canEditWhenComplete(false)
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        val scenario = launchActivity(form)
        scenario.onActivity {
            val viewModel = ViewModelProvider(it)[SelectionMapViewModel::class.java]
            val items = viewModel.getMappableItems().value

            assertThat(items[0].action, equalTo(null))
            assertThat(
                items[0].info,
                equalTo(application.getString(R.string.cannot_edit_completed_form))
            )

            assertThat(items[1].action, equalTo(null))
            assertThat(
                items[1].info,
                equalTo(application.getString(R.string.cannot_edit_completed_form))
            )

            assertThat(items[2].action, equalTo(null))
            assertThat(
                items[2].info,
                equalTo(application.getString(R.string.cannot_edit_completed_form))
            )
        }
    }

    private fun launchActivity(form: Form): ActivityScenario<FormMapActivity> {
        val intent = Intent(application, FormMapActivity::class.java).also {
            it.putExtra(FormMapActivity.EXTRA_FORM_ID, form.dbId)
        }
        val scenario = launcherRule.launch<FormMapActivity>(intent)
        return scenario
    }

    private fun formatDate(string: Int, date: Long): String {
        return SimpleDateFormat(
            application.getString(string),
            Locale.getDefault()
        ).format(date)
    }
}
