package org.odk.collect.android.widgets.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.items.IconifiedText
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.geo.selection.SelectionMapData
import org.odk.collect.icons.R

class SelectOneFromMapData(
    scheduler: Scheduler,
    prompt: FormEntryPrompt,
    private val selectChoiceLoader: SelectChoiceLoader,
    private val selectedIndex: Int?
) : ViewModel(), SelectionMapData {

    private val mapTitle = MutableLiveData(prompt.longText)
    private val itemCount = MutableNonNullLiveData(0)
    private val items = MutableLiveData<List<MappableItem>>(emptyList())
    private val isLoading = MutableNonNullLiveData(true)

    init {
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
                DisplayString.Resource(org.odk.collect.strings.R.string.select_item)
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

    override fun getItemType(): DisplayString {
        return DisplayString.Resource(org.odk.collect.strings.R.string.choices)
    }

    override fun getItemCount(): NonNullLiveData<Int> {
        return itemCount
    }

    override fun isSelected(mappableItem: MappableItem): Boolean {
        return mappableItem.id == selectedIndex?.toLong()
    }

    override fun getMappableItems(): LiveData<List<MappableItem>> {
        return items
    }
}
