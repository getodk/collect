package org.odk.collect.android.geo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.androidshared.ui.ToastUtils.showShortToast
import org.odk.collect.geo.R
import org.odk.collect.maps.layers.ReferenceLayer
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import java.io.File
import java.util.Locale
import javax.annotation.Nullable
import javax.inject.Inject

class OfflineMapLayerSelectionFragment : BottomSheetDialogFragment() {

    private val viewModel: OfflineMapLayerViewModel by viewModels()
    private val PICKFILE_RESULT_CODE = 1

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private lateinit var adapter: OfflineMapLayersAdapter
    private val supportedLayers: MutableList<ReferenceLayer> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_offline_map_selection, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.offlineMapLayerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the layers list
        initializeLayersList()

        val referenceLayerId = settingsProvider.getUnprotectedSettings().getString("reference_layer")
                ?: ""

        adapter = OfflineMapLayersAdapter(
                layers = supportedLayers,
                referenceLayerId = referenceLayerId,
                onSelectLayerListener = { referenceLayer ->
                    onFeatureClicked(referenceLayer)
                },
                onDeleteLayerListener = { referenceLayer ->
                    onDeleteLayer(referenceLayer)
                }
        )
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.add_layer_button).setOnClickListener {
            val intent = FileUtils.openFilePickerForMbtiles()
            startActivityForResult(intent, PICKFILE_RESULT_CODE)
        }
    }

    private fun initializeLayersList() {
        val cftor = MapConfiguratorProvider.getConfigurator()
        val noneReferenceLayer = ReferenceLayer(id = "none", file = File(""))

        supportedLayers.clear()
        supportedLayers.add(noneReferenceLayer)
        for (layer in referenceLayerRepository.getAll()) {
            if (cftor.supportsLayer(layer.file)) {
                supportedLayers.add(layer)
            }
        }
    }

    private fun onDeleteLayer(referenceLayer: ReferenceLayer) {
        val dialogView = LayoutInflater.from(context).inflate(org.odk.collect.android.R.layout.delete_layer_dialog_layout, null)

        val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

        dialogView.findViewById<Button>(org.odk.collect.android.R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(org.odk.collect.android.R.id.deleteButton).setOnClickListener {
            dialog.dismiss()
            viewModel.deleteLayer(referenceLayer.file)
            val position = supportedLayers.indexOf(referenceLayer)
            if (position != -1) {
                supportedLayers.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        }

        dialog.show()

    }

    private fun onFeatureClicked(referenceLayer: ReferenceLayer) {
        settingsProvider.getUnprotectedSettings().save("reference_layer", referenceLayer.id)
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedFileUri = data.data ?: return
            val fileName = FileUtils.getFileNameFromContentUri(requireContext().contentResolver, selectedFileUri)
            if (fileName == null || !fileName.trim { it <= ' ' }.lowercase(Locale.getDefault()).endsWith(".mbtiles")) {
                showShortToast(requireContext(), "Import failed. Invalid file format.")
                return
            }
            try {
                val destFile = File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS), fileName)
                FileUtils.saveLayersFromUri(selectedFileUri, destFile, requireContext())
                showLongToast(requireContext(), "Import successful. You can select the layer from the layer switcher.")
                // Update the layers list and refresh the adapter
                initializeLayersList()
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                showShortToast(requireContext(), "An error occurred during import. Please try again.")
            }
        }
    }

    companion object {
        fun showBottomSheet(supportFragmentManager: FragmentManager) {
            val bottomSheetFragment = OfflineMapLayerSelectionFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }
}

class OfflineMapLayerViewModel : ViewModel() {

    fun deleteLayer(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                FileUtils.deleteAndReport(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
