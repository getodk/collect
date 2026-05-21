package org.odk.collect.android.widgets.items

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.items.IconifiedText
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.geo.selection.SelectionMapData
import org.odk.collect.icons.R

class SelectOneFromMapData(
    private val resources: Resources,
    scheduler: Scheduler,
    prompt: FormEntryPrompt,
    private val selectChoiceLoader: SelectChoiceLoader,
    private val selectedIndex: Int?
) : SelectionMapData {

    private val mapTitle = MutableLiveData(prompt.longText)
    private val itemCount = MutableNonNullLiveData(0)
    private val items = MutableLiveData<List<MappableItem>?>(null)
    private val isLoading = MutableNonNullLiveData(true)

    init {
        isLoading.value = true

        scheduler.immediate(
            background = {
                loadItemsFromChoices(prompt)
            },
            foreground = {
                itemCount.value = it.first
                items.value = it.second
                isLoading.value = false
            }
        )
    }

    private fun loadItemsFromChoices(prompt: FormEntryPrompt): Pair<Int, List<MappableItem>> {
        val selectChoices = selectChoiceLoader.loadSelectChoices(prompt)

        val options = MappableItemsParser.Options(
            action = IconifiedText(
                R.drawable.ic_save,
                resources.getString(org.odk.collect.strings.R.string.select_item)
            )
        )
        val mappableItems =
            MappableItemsParser.parseChoices(selectChoices, options, prompt::getSelectChoiceText)

        return Pair(selectChoices.size, mappableItems)
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return isLoading
    }

    override fun getMapTitle(): LiveData<String?> {
        return mapTitle
    }

    override fun getItemType(): String {
        return resources.getString(org.odk.collect.strings.R.string.choices)
    }

    override fun getItemCount(): NonNullLiveData<Int> {
        return itemCount
    }

    override fun isSelected(mappableItem: MappableItem): Boolean {
        return mappableItem.id == selectedIndex?.toLong()
    }

    override fun getMappableItems(): LiveData<List<MappableItem>?> {
        return items
    }
}