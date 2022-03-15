package org.odk.collect.android.formmanagement.formmap

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.R
import org.odk.collect.android.external.InstanceProvider
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.geo.MappableSelectItem
import org.odk.collect.geo.SelectionMapViewModel
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormMapViewModel(
    private val resources: Resources,
    formId: Long,
    formsRepository: FormsRepository,
    private val instancesRepository: InstancesRepository,
    private val settingsProvider: SettingsProvider,
) : SelectionMapViewModel() {

    private var mapTitle = MutableLiveData<String>()
    private var mappableItems = MutableNonNullLiveData<List<MappableSelectItem>>(emptyList())
    private var itemCount = MutableLiveData(0)
    private val form = formsRepository.get(formId)!!

    init {
        mapTitle.value = form.displayName
    }

    override fun getMapTitle(): LiveData<String> {
        return mapTitle
    }

    override fun getItemCount(): LiveData<Int> {
        return itemCount
    }

    override fun getMappableItems(): NonNullLiveData<List<MappableSelectItem>> {
        return mappableItems
    }

    fun load() {
        val instances = instancesRepository.getAllByFormId(form.formId)
        val items: MutableList<MappableSelectItem> = ArrayList()

        for (instance in instances) {
            if (instance.geometry != null && instance.geometryType == "Point") {
                try {
                    val geometry = JSONObject(instance.geometry)
                    val coordinates = geometry.getJSONArray("coordinates")

                    // In GeoJSON, longitude comes before latitude.
                    val lon = coordinates.getDouble(0)
                    val lat = coordinates.getDouble(1)

                    items.add(convertItem(instance, lat, lon, resources))
                } catch (e: JSONException) {
                    Timber.w("Invalid JSON in instances table: %s", instance.geometry)
                }
            }
        }

        mappableItems.value = items
        itemCount.value = instances.size
    }

    private fun convertItem(
        instance: Instance,
        latitude: Double,
        longitude: Double,
        resources: Resources
    ): MappableSelectItem {
        val instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(
            resources,
            instance.status,
            Date(instance.lastStatusChangeDate)
        )

        val info = when {
            instance.deletedDate != null -> {
                val deletedTime = resources.getString(R.string.deleted_on_date_at_time)
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
                resources.getString(R.string.cannot_edit_completed_form)
            }

            else -> null
        }

        val canEditSaved = settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)

        val editAction = MappableSelectItem.IconifiedText(
            if (canEditSaved) R.drawable.ic_edit else R.drawable.ic_visibility,
            resources.getString(if (canEditSaved) R.string.review_data else R.string.view_data)
        )

        val viewAction = MappableSelectItem.IconifiedText(
            R.drawable.ic_visibility,
            resources.getString(R.string.view_data)
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
