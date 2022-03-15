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

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.R
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
import org.odk.collect.geo.SelectionMapFragment
import org.odk.collect.geo.SelectionMapViewModel
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
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

    private val form: Form by lazy {
        formsRepositoryProvider.get()[intent.getLongExtra(EXTRA_FORM_ID, -1)]!!
    }

    private val selectionMapViewModel: SelectionMapViewModel by lazy {
        ViewModelProvider(this)[SelectionMapViewModel::class.java]
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.form_map_activity)

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

        selectionMapViewModel.setMapTitle(form.displayName)

        val loader =
            FormMapInstancesLoader(this, instancesRepositoryProvider.get(), settingsProvider)
        val (instancesCount, mappableItems) = loader.load(form.formId)
        selectionMapViewModel.setItems(instancesCount, mappableItems)
    }

    companion object {
        const val EXTRA_FORM_ID = "form_id"
    }
}

private class FormMapInstancesLoader(
    private val context: Context,
    private val instancesRepository: InstancesRepository,
    private val settingsProvider: SettingsProvider
) {

    fun load(formId: String): Pair<Int, List<MappableSelectItem>> {
        val instances = instancesRepository.getAllByFormId(formId)
        val items: MutableList<MappableSelectItem> = ArrayList()

        for (instance in instances) {
            if (instance.geometry != null && instance.geometryType == "Point") {
                try {
                    val geometry = JSONObject(instance.geometry)
                    val coordinates = geometry.getJSONArray("coordinates")

                    // In GeoJSON, longitude comes before latitude.
                    val lon = coordinates.getDouble(0)
                    val lat = coordinates.getDouble(1)

                    items.add(convertItem(instance, lat, lon, context))
                } catch (e: JSONException) {
                    Timber.w("Invalid JSON in instances table: %s", instance.geometry)
                }
            }
        }

        return Pair(instances.size, items)
    }

    private fun convertItem(
        instance: Instance,
        latitude: Double,
        longitude: Double,
        context: Context
    ): MappableSelectItem {
        val instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(
            context,
            instance.status,
            Date(instance.lastStatusChangeDate)
        )

        val info = when {
            instance.deletedDate != null -> {
                val deletedTime = context.getString(R.string.deleted_on_date_at_time)
                val dateFormat = SimpleDateFormat(
                    deletedTime,
                    Locale.getDefault()
                )

                dateFormat.format(instance.deletedDate)
            }

            !instance.canEditWhenComplete() && listOf(
                Instance.STATUS_COMPLETE,
                Instance.STATUS_SUBMISSION_FAILED,
                Instance.STATUS_SUBMITTED
            ).contains(instance.status) -> {
                context.getString(R.string.cannot_edit_completed_form)
            }

            else -> null
        }

        val canEditSaved = settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)

        val editAction = MappableSelectItem.IconifiedText(
            if (canEditSaved) R.drawable.ic_edit else R.drawable.ic_visibility,
            context.getString(if (canEditSaved) R.string.review_data else R.string.view_data)
        )

        val viewAction = MappableSelectItem.IconifiedText(
            R.drawable.ic_visibility,
            context.getString(R.string.view_data)
        )

        val action = if (instance.deletedDate != null) {
            null
        } else {
            when (instance.status) {
                Instance.STATUS_INCOMPLETE -> {
                    editAction
                }

                Instance.STATUS_COMPLETE -> {
                    if (instance.canEditWhenComplete()) {
                        editAction
                    } else {
                        null
                    }
                }

                Instance.STATUS_SUBMISSION_FAILED,
                Instance.STATUS_SUBMITTED -> {
                    if (instance.canEditWhenComplete()) {
                        viewAction
                    } else {
                        null
                    }
                }

                else -> throw IllegalArgumentException()
            }
        }

        return MappableSelectItem(
            instance.dbId,
            latitude,
            longitude,
            getDrawableIdForStatus(instance.status, false),
            getDrawableIdForStatus(instance.status, true),
            instance.displayName,
            MappableSelectItem.IconifiedText(
                getSubmissionSummaryStatusIcon(instance.status),
                instanceLastStatusChangeDate
            ),
            info,
            action
        )
    }

    private fun getDrawableIdForStatus(status: String, enlarged: Boolean): Int {
        when (status) {
            Instance.STATUS_INCOMPLETE -> return if (enlarged) R.drawable.ic_room_form_state_incomplete_48dp else R.drawable.ic_room_form_state_incomplete_24dp
            Instance.STATUS_COMPLETE -> return if (enlarged) R.drawable.ic_room_form_state_complete_48dp else R.drawable.ic_room_form_state_complete_24dp
            Instance.STATUS_SUBMITTED -> return if (enlarged) R.drawable.ic_room_form_state_submitted_48dp else R.drawable.ic_room_form_state_submitted_24dp
            Instance.STATUS_SUBMISSION_FAILED -> return if (enlarged) R.drawable.ic_room_form_state_submission_failed_48dp else R.drawable.ic_room_form_state_submission_failed_24dp
        }

        return R.drawable.ic_map_point
    }

    private fun getSubmissionSummaryStatusIcon(instanceStatus: String?): Int {
        when (instanceStatus) {
            Instance.STATUS_INCOMPLETE -> return R.drawable.form_state_saved
            Instance.STATUS_COMPLETE -> return R.drawable.form_state_finalized
            Instance.STATUS_SUBMITTED -> return R.drawable.form_state_submited
            Instance.STATUS_SUBMISSION_FAILED -> return R.drawable.form_state_submission_failed
        }

        throw IllegalArgumentException()
    }
}
