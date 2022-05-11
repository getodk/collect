package org.odk.collect.android.formmanagement.formmap

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.R
import org.odk.collect.android.external.InstanceProvider
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.geo.selection.SelectionMapData
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormMapViewModel(
    private val resources: Resources,
    private val formId: Long,
    private val formsRepository: FormsRepository,
    private val instancesRepository: InstancesRepository,
    private val settingsProvider: SettingsProvider,
    private val scheduler: Scheduler
) : ViewModel(), SelectionMapData {

    private var _form: Form? = null

    private val mapTitle = MutableLiveData<String?>()
    private var mappableItems = MutableLiveData<List<MappableSelectItem>>(null)
    private var itemCount = MutableNonNullLiveData(0)
    private val isLoading = MutableNonNullLiveData(false)

    override fun getMapTitle(): LiveData<String?> {
        return mapTitle
    }

    override fun getItemType(): String {
        return resources.getString(R.string.saved_forms)
    }

    override fun getItemCount(): NonNullLiveData<Int> {
        return itemCount
    }

    override fun getMappableItems(): LiveData<List<MappableSelectItem>?> {
        return mappableItems
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return isLoading
    }

    fun load() {
        isLoading.value = true

        scheduler.immediate(
            background = {
                val form = _form ?: formsRepository.get(formId)!!.also { _form = it }
                val instances = instancesRepository.getAllByFormId(form.formId)
                val items = mutableListOf<MappableSelectItem>()

                for (instance in instances) {
                    if (instance.geometry != null && instance.geometryType == Instance.GEOMETRY_TYPE_POINT) {
                        try {
                            val geometry = JSONObject(instance.geometry)
                            val coordinates = geometry.getJSONArray("coordinates")

                            // In GeoJSON, longitude comes before latitude.
                            val lon = coordinates.getDouble(0)
                            val lat = coordinates.getDouble(1)

                            items.add(createItem(instance, lat, lon))
                        } catch (e: JSONException) {
                            Timber.w("Invalid JSON in instances table: %s", instance.geometry)
                        }
                    }
                }

                Triple(form.displayName, items, instances.size)
            },
            foreground = {
                mapTitle.value = it.first
                mappableItems.value = it.second as List<MappableSelectItem>
                itemCount.value = it.third
                isLoading.value = false
            }
        )
    }

    private fun createItem(
        instance: Instance,
        latitude: Double,
        longitude: Double,
    ): MappableSelectItem {
        val instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(
            resources,
            instance.status,
            Date(instance.lastStatusChangeDate)
        )

        return if (instance.deletedDate != null) {
            val deletedTime = resources.getString(R.string.deleted_on_date_at_time)
            val dateFormat = SimpleDateFormat(
                deletedTime,
                Locale.getDefault()
            )

            val info = dateFormat.format(instance.deletedDate)
            MappableSelectItem.WithInfo(
                instance.dbId,
                latitude,
                longitude,
                getDrawableIdForStatus(instance.status, false),
                getDrawableIdForStatus(instance.status, true),
                instance.displayName,
                listOf(
                    MappableSelectItem.IconifiedText(
                        getSubmissionSummaryStatusIcon(instance.status),
                        instanceLastStatusChangeDate
                    )
                ),
                info
            )
        } else if (!instance.canEditWhenComplete() && listOf(
                Instance.STATUS_COMPLETE,
                Instance.STATUS_SUBMISSION_FAILED,
                Instance.STATUS_SUBMITTED
            ).contains(instance.status)
        ) {
            val info = resources.getString(R.string.cannot_edit_completed_form)
            MappableSelectItem.WithInfo(
                instance.dbId,
                latitude,
                longitude,
                getDrawableIdForStatus(instance.status, false),
                getDrawableIdForStatus(instance.status, true),
                instance.displayName,
                listOf(
                    MappableSelectItem.IconifiedText(
                        getSubmissionSummaryStatusIcon(instance.status),
                        instanceLastStatusChangeDate
                    )
                ),
                info
            )
        } else {
            val action = when (instance.status) {
                Instance.STATUS_INCOMPLETE -> createEditAction()
                Instance.STATUS_COMPLETE -> createEditAction()
                else -> createViewAction()
            }

            MappableSelectItem.WithAction(
                instance.dbId,
                latitude,
                longitude,
                getDrawableIdForStatus(instance.status, false),
                getDrawableIdForStatus(instance.status, true),
                instance.displayName,
                listOf(
                    MappableSelectItem.IconifiedText(
                        getSubmissionSummaryStatusIcon(instance.status),
                        instanceLastStatusChangeDate
                    )
                ),
                action
            )
        }
    }

    private fun createViewAction(): MappableSelectItem.IconifiedText {
        return MappableSelectItem.IconifiedText(
            R.drawable.ic_visibility,
            resources.getString(R.string.view_data)
        )
    }

    private fun createEditAction(): MappableSelectItem.IconifiedText {
        val canEditSaved = settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)

        return MappableSelectItem.IconifiedText(
            if (canEditSaved) R.drawable.ic_edit else R.drawable.ic_visibility,
            resources.getString(if (canEditSaved) R.string.review_data else R.string.view_data)
        )
    }

    private fun getDrawableIdForStatus(status: String, enlarged: Boolean): Int {
        return when (status) {
            Instance.STATUS_INCOMPLETE -> if (enlarged) R.drawable.ic_room_form_state_incomplete_48dp else R.drawable.ic_room_form_state_incomplete_24dp
            Instance.STATUS_COMPLETE -> if (enlarged) R.drawable.ic_room_form_state_complete_48dp else R.drawable.ic_room_form_state_complete_24dp
            Instance.STATUS_SUBMITTED -> if (enlarged) R.drawable.ic_room_form_state_submitted_48dp else R.drawable.ic_room_form_state_submitted_24dp
            Instance.STATUS_SUBMISSION_FAILED -> if (enlarged) R.drawable.ic_room_form_state_submission_failed_48dp else R.drawable.ic_room_form_state_submission_failed_24dp
            else -> R.drawable.ic_map_point
        }
    }

    private fun getSubmissionSummaryStatusIcon(instanceStatus: String?): Int {
        return when (instanceStatus) {
            Instance.STATUS_COMPLETE -> R.drawable.form_state_finalized
            Instance.STATUS_SUBMITTED -> R.drawable.form_state_submited
            Instance.STATUS_SUBMISSION_FAILED -> R.drawable.form_state_submission_failed
            else -> R.drawable.form_state_saved
        }
    }
}
