package org.odk.collect.android.geo

import OfflineMapLayersAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.geo.R
import org.odk.collect.maps.layers.ReferenceLayer
import org.odk.collect.maps.layers.ReferenceLayerRepository
import java.io.File
import java.util.Locale
import javax.annotation.Nullable
import javax.inject.Inject


data class OfflineMapLayerItem(
        val name: String,
        val filePath: String,
        var isSelected: Boolean = false
)

class OfflineMapLayerSelectionFragment(
        private val selectionMapData: MapLayerSelectionData, // Declaration of selectionMapData
        private val itemList: List<OfflineMapLayerItem>,
        private val onBackPressedDispatcher: (() -> OnBackPressedDispatcher)? = null
) : BottomSheetDialogFragment() {

    private val PICKFILE_RESULT_CODE = 1

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    private val selectedViewModel by viewModels<SelectedMapItemViewModel>()
    private val featureIdsByItemId: MutableMap<Long, Int> = mutableMapOf()
    private val itemsByFeatureId: MutableMap<Int, OfflineMapLayerItem> = mutableMapOf()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_offline_map_selection, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.offlineMapLayerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)  // Use a linear layout manager

        val cftor = MapConfiguratorProvider.getConfigurator()
        val supportedLayers: MutableList<ReferenceLayer> = ArrayList()
        for (layer in referenceLayerRepository.getAll()) {
            if (cftor.supportsLayer(layer.file)) {
                supportedLayers.add(layer)
            }
        }

        val selectedLayerIndex = 0
        val adapter = OfflineMapLayersAdapter(supportedLayers, selectedLayerIndex) { referenceLayer ->
            onFeatureClicked(referenceLayer)


        }
        recyclerView.adapter = adapter


        view.findViewById<Button>(R.id.add_layer_button).setOnClickListener {
            val intent = FileUtils.openFilePickerForMbtiles()
            startActivityForResult(intent, PICKFILE_RESULT_CODE)
        }
    }


    fun updatePreference(path: String) {
        val sharedPreferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        with(sharedPreferences!!.edit()) {
            putString("reference_layer", path)
            apply()
        }
    }

    private fun onFeatureClicked(referenceLayer: ReferenceLayer) {
        val path = referenceLayer.file.absolutePath
        val cftor = MapConfiguratorProvider.getConfigurator()
        cftor.supportsLayer(referenceLayer.file)

        cftor.getDisplayName(File(path))

        updatePreference(path)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICKFILE_RESULT_CODE || resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        val selectedFileUri = data.data ?: return
        val fileName = FileUtils.getFileNameFromContentUri(requireContext().contentResolver, selectedFileUri)
        if (fileName == null || !fileName.trim { it <= ' ' }.lowercase(Locale.getDefault()).endsWith(".mbtiles")) {
            // Show bad file format toast
//            showShortToast(requireContext(), getString(android.R.string.mb_tiles_import_bad_file_format))
            return
        }
        try {
            val destFile = File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS), fileName)
            FileUtils.saveLayersFromUri(selectedFileUri, destFile, requireContext())
//            showLongToast(requireContext(), getString(R.string.mb_tiles_import_was_successful))

        } catch (e: Exception) {
            e.printStackTrace()
//            showShortToast(requireContext(), getString(R.string.mb_tiles_import_failed))
        }
    }

    companion object {

        val selectionMapData = object : MapLayerSelectionData {

            override fun isLoading(): NonNullLiveData<Boolean> {
                TODO("Not yet implemented")
            }

            override fun getMapTitle(): LiveData<String?> {
                TODO("Not yet implemented")
            }

            override fun getItemType(): String {
                TODO("Not yet implemented")
            }

            override fun getItemCount(): NonNullLiveData<Int> {
                TODO("Not yet implemented")
            }

            override fun getItems(): LiveData<List<OfflineMapLayerItem>>? {
                TODO("Not yet implemented")
            }

            override fun onItemSelectionChanged(item: OfflineMapLayerItem) {
                TODO("Not yet implemented")
            }
        }

        val itemList = listOf(
                OfflineMapLayerItem("Map 1", "/path/to/map1", false),
                OfflineMapLayerItem("Map 2", "/path/to/map2", false),
                // Add more OfflineMapLayerItem instances as needed
        )

        fun showBottomSheet(supportFragmentManager: FragmentManager) {
            val bottomSheetFragment = OfflineMapLayerSelectionFragment(selectionMapData, itemList)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }


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
