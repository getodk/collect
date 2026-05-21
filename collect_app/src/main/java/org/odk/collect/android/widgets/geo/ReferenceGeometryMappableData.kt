package org.odk.collect.android.widgets.geo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import org.odk.collect.android.widgets.items.MappableItemsParser
import org.odk.collect.androidshared.async.TrackableWorker
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.items.MappableData
import org.odk.collect.geo.items.MappableItem

class ReferenceGeometryMappableDate(
    scheduler: Scheduler,
    prompt: FormEntryPrompt,
    private val selectChoiceLoader: SelectChoiceLoader,
) : MappableData {

    private val trackableWorker = TrackableWorker(scheduler)
    private val items = MutableLiveData<List<MappableItem>?>()

    init {
        trackableWorker.immediate(
            background = {
                val selectChoices = selectChoiceLoader.loadSelectChoices(prompt)
                val options = MappableItemsParser.Options()
                MappableItemsParser.parseChoices(
                    selectChoices,
                    options,
                    prompt::getSelectChoiceText
                )
            },
            foreground = {
                items.value = it
            }
        )
    }

    override fun getMappableItems(): LiveData<List<MappableItem>?> {
        return items
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return trackableWorker.isWorking
    }
}