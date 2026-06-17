package org.odk.collect.android.widgets.geo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import org.odk.collect.android.widgets.items.MappableItemsParser
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.DispatcherProvider
import org.odk.collect.geo.items.MappableData
import org.odk.collect.geo.items.MappableItem

class ReferenceGeometryMappableData(
    dispatcherProvider: DispatcherProvider,
    prompt: FormEntryPrompt,
    private val selectChoiceLoader: SelectChoiceLoader,
) : ViewModel(), MappableData {

    private val isLoading = MutableNonNullLiveData(false)
    private val items = MutableLiveData<List<MappableItem>>(emptyList())

    init {

        viewModelScope.launch(dispatcherProvider.foreground) {
            isLoading.value = true

            val mappableItems = withContext(dispatcherProvider.background) {
                val selectChoices = selectChoiceLoader.loadSelectChoices(prompt)
                val options = MappableItemsParser.Options(color = ITEM_COLOR)
                MappableItemsParser.parseChoices(
                    selectChoices,
                    options,
                    prompt::getSelectChoiceText
                )
            }

            items.value = mappableItems
            isLoading.value = false
        }
    }

    override fun getMappableItems(): LiveData<List<MappableItem>> {
        return items
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return isLoading
    }

    companion object {
        const val ITEM_COLOR = "#5e5284"
    }
}