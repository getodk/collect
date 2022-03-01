/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.R
import org.odk.collect.android.activities.viewmodels.FormMapViewModel
import org.odk.collect.android.activities.viewmodels.FormMapViewModel.ClickAction
import org.odk.collect.android.activities.viewmodels.FormMapViewModel.MappableFormInstance
import org.odk.collect.android.external.InstanceProvider
import org.odk.collect.android.formmanagement.FormNavigator
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.geo.MappableSelectItem
import org.odk.collect.geo.MappableSelectItem.IconifiedText
import org.odk.collect.geo.SelectionMapFragment
import org.odk.collect.geo.SelectionMapViewModel
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Show a map with points representing saved instances of the selected form.
 */
class FormMapActivity : LocalizedActivity() {

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    private lateinit var formMapViewModel: FormMapViewModel
    private lateinit var selectionMapViewModel: SelectionMapViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.form_map_activity)

        val form = loadForm()

        selectionMapViewModel = ViewModelProvider(this).get(
            SelectionMapViewModel::class.java
        )
        selectionMapViewModel.setMapTitle(form.displayName)

        val formNavigator = FormNavigator(
            currentProjectProvider,
            settingsProvider,
            instancesRepositoryProvider::get
        )

        supportFragmentManager.setFragmentResultListener(
            SelectionMapFragment.REQUEST_SELECT_ITEM,
            this
        ) { _: String?, result: Bundle ->
            if (result.containsKey(SelectionMapFragment.RESULT_SELECTED_ITEM)) {
                val instanceId = result.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM)
                formNavigator.editInstance(this, instanceId)
            } else if (result.containsKey(SelectionMapFragment.RESULT_CREATE_NEW_ITEM)) {
                formNavigator.newInstance(this, form.dbId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        selectionMapViewModel.setItems(getTotalInstanceCount(), getItems())
    }

    private fun loadForm(): Form {
        val form = formsRepositoryProvider.get()[intent.getLongExtra(EXTRA_FORM_ID, -1)]

        val viewModelFactory = FormMapViewModelFactory(form!!, instancesRepositoryProvider.get())
        formMapViewModel = ViewModelProvider(this, viewModelFactory)[FormMapViewModel::class.java]

        return form
    }

    private fun getItems(): List<MappableSelectItem> {
        val instances = formMapViewModel.mappableFormInstances
        val items: MutableList<MappableSelectItem> = ArrayList()
        for (instance in instances) {
            items.add(convertItem(instance))
        }
        return items
    }

    private fun getTotalInstanceCount(): Int = formMapViewModel.totalInstanceCount

    private fun convertItem(mappableFormInstance: MappableFormInstance): MappableSelectItem {
        val instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(
            this,
            mappableFormInstance.status,
            mappableFormInstance.lastStatusChangeDate
        )

        val info = when (mappableFormInstance.clickAction) {
            ClickAction.DELETED_TOAST -> {
                val deletedTime = getString(R.string.deleted_on_date_at_time)
                val dateFormat = SimpleDateFormat(
                    deletedTime,
                    Locale.getDefault()
                )

                dateFormat.format(formMapViewModel.getDeletedDateOf(mappableFormInstance.databaseId))
            }

            ClickAction.NOT_VIEWABLE_TOAST -> getString(R.string.cannot_edit_completed_form)
            else -> null
        }
        val action = when (mappableFormInstance.clickAction) {
            ClickAction.OPEN_READ_ONLY -> IconifiedText(
                R.drawable.ic_visibility, getString(R.string.view_data)
            )

            ClickAction.OPEN_EDIT -> {
                val canEditSaved = settingsProvider.getProtectedSettings()
                    .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)

                IconifiedText(
                    if (canEditSaved) R.drawable.ic_edit else R.drawable.ic_visibility,
                    getString(if (canEditSaved) R.string.review_data else R.string.view_data)
                )
            }

            else -> null
        }

        return MappableSelectItem(
            mappableFormInstance.databaseId,
            mappableFormInstance.latitude,
            mappableFormInstance.longitude,
            getDrawableIdForStatus(mappableFormInstance.status, false),
            getDrawableIdForStatus(mappableFormInstance.status, true),
            mappableFormInstance.instanceName,
            IconifiedText(
                getSubmissionSummaryStatusIcon(mappableFormInstance.status),
                instanceLastStatusChangeDate
            ),
            info,
            action
        )
    }

    fun onFeatureClicked(featureId: Int) {
        val fragment = supportFragmentManager.fragments.stream()
            .filter { fragment1: Fragment -> fragment1.javaClass == SelectionMapFragment::class.java }
            .findFirst().get()
        (fragment as SelectionMapFragment).onFeatureClicked(featureId)
    }

    private inner class FormMapViewModelFactory(
        private val form: Form,
        private val instancesRepository: InstancesRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormMapViewModel(form, instancesRepository) as T
        }
    }

    companion object {
        const val EXTRA_FORM_ID = "form_id"

        private fun getDrawableIdForStatus(status: String, enlarged: Boolean): Int {
            when (status) {
                Instance.STATUS_INCOMPLETE -> return if (enlarged) R.drawable.ic_room_form_state_incomplete_48dp else R.drawable.ic_room_form_state_incomplete_24dp
                Instance.STATUS_COMPLETE -> return if (enlarged) R.drawable.ic_room_form_state_complete_48dp else R.drawable.ic_room_form_state_complete_24dp
                Instance.STATUS_SUBMITTED -> return if (enlarged) R.drawable.ic_room_form_state_submitted_48dp else R.drawable.ic_room_form_state_submitted_24dp
                Instance.STATUS_SUBMISSION_FAILED -> return if (enlarged) R.drawable.ic_room_form_state_submission_failed_48dp else R.drawable.ic_room_form_state_submission_failed_24dp
            }

            return R.drawable.ic_map_point
        }

        fun getSubmissionSummaryStatusIcon(instanceStatus: String?): Int {
            when (instanceStatus) {
                Instance.STATUS_INCOMPLETE -> return R.drawable.form_state_saved
                Instance.STATUS_COMPLETE -> return R.drawable.form_state_finalized
                Instance.STATUS_SUBMITTED -> return R.drawable.form_state_submited
                Instance.STATUS_SUBMISSION_FAILED -> return R.drawable.form_state_submission_failed
            }

            throw IllegalArgumentException()
        }
    }
}
