package org.odk.collect.geo.selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.geo.R

data class OfflineMapLayerItem(
        val name: String,
        val filePath: String,
        var isSelected: Boolean = false
)

class OfflineMapLayerSelectionFragment(
        private val selectionMapData: MapLayerSelectionData, // Declaration of selectionMapData
        private val itemList: List<OfflineMapLayerItem>,
        private val onBackPressedDispatcher: (() -> OnBackPressedDispatcher)? = null
) : Fragment() {

    private val selectedViewModel by viewModels<SelectedMapItemViewModel>()
    private val featureIdsByItemId: MutableMap<Long, Int> = mutableMapOf()
    private val itemsByFeatureId: MutableMap<Int, OfflineMapLayerItem> = mutableMapOf()

    // Other injected dependencies here

    // Other variables and constants here

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.selection_map_layout, container, false)

        // Initialize views and UI components

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize map and other components
        // Set up listeners and UI interactions
    }

    private fun onFeatureClicked(featureId: Int, maintainZoom: Boolean = true) {
        // Handle feature click on the map to update item selection status
        val item = itemsByFeatureId[featureId]
        item?.let {
            it.isSelected = !it.isSelected
            selectionMapData.onItemSelectionChanged(it)
            // Handle UI updates or other actions based on item selection
        }
    }

    // Other lifecycle methods and helper functions here

    companion object {
        const val REQUEST_SELECT_ITEM = "select_item"
        const val RESULT_SELECTED_ITEM = "selected_item"
        const val RESULT_CREATE_NEW_ITEM = "create_new_item"
    }
}

internal class SelectedMapItemViewModel : ViewModel() {
    private var selectedItem: OfflineMapLayerItem? = null

    fun getSelectedItem(): OfflineMapLayerItem? {
        return selectedItem
    }

    fun setSelectedItem(item: OfflineMapLayerItem?) {
        selectedItem = item
    }
}

interface MapLayerSelectionData {
    fun isLoading(): NonNullLiveData<Boolean>
    fun getMapTitle(): LiveData<String?>
    fun getItemType(): String
    fun getItemCount(): NonNullLiveData<Int>
    fun getItems(): LiveData<List<OfflineMapLayerItem>>?
    fun onItemSelectionChanged(item: OfflineMapLayerItem)
}
