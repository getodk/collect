package org.odk.collect.maps.layers

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.livedata.LiveDataUtils
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.addOnClickListener
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBinding
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.webpage.ExternalWebPageHelper

class OfflineMapLayersPicker(
    registry: ActivityResultRegistry,
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider,
    private val externalWebPageHelper: ExternalWebPageHelper
) : BottomSheetDialogFragment(),
    OfflineMapLayersPickerAdapter.OfflineMapLayersPickerAdapterInterface {
    private val stateViewModel: OfflineMapLayersStateViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineMapLayersStateViewModel(settingsProvider) as T
            }
        }
    }

    private val sharedViewModel: OfflineMapLayersViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineMapLayersViewModel(referenceLayerRepository, scheduler, settingsProvider) as T
            }
        }
    }

    private lateinit var binding: OfflineMapLayersPickerBinding

    private val getLayers = registerForActivityResult(ActivityResultContracts.GetMultipleContents(), registry) { uris ->
        if (uris.isNotEmpty()) {
            sharedViewModel.loadLayersToImport(uris, requireContext())
            DialogFragmentUtils.showIfNotShowing(
                OfflineMapLayersImporter::class.java,
                childFragmentManager
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(OfflineMapLayersImporter::class) {
                OfflineMapLayersImporter(referenceLayerRepository, scheduler, settingsProvider)
            }
            .build()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OfflineMapLayersPickerBinding.inflate(inflater)

        binding.mbtilesInfoGroup.addOnClickListener {
            externalWebPageHelper.openWebPageInCustomTab(
                requireActivity(),
                Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices")
            )
        }

        binding.addLayer.setOnClickListener {
            getLayers.launch("*/*")
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.save.setOnClickListener {
            sharedViewModel.saveCheckedLayer(stateViewModel.getCheckedLayer())
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressIndicator.visibility = View.VISIBLE
                binding.layers.visibility = View.GONE
                binding.save.isEnabled = false
            } else {
                binding.progressIndicator.visibility = View.GONE
                binding.layers.visibility = View.VISIBLE
                binding.save.isEnabled = true
            }
        }

        val adapter = OfflineMapLayersPickerAdapter(this)
        binding.layers.setAdapter(adapter)
        LiveDataUtils.zip3(
            sharedViewModel.existingLayers,
            stateViewModel.checkedLayerId,
            stateViewModel.expandedLayerIds
        ).observe(this) { (layers, checkedLayerId, expandedLayerIds) ->
            updateAdapter(layers, checkedLayerId, expandedLayerIds, adapter)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onLayerChecked(layerId: String?) {
        stateViewModel.onLayerChecked(layerId)
    }

    override fun onLayerToggled(layerId: String?) {
        stateViewModel.onLayerToggled(layerId)
    }

    override fun onDeleteLayer(layerItem: CheckableReferenceLayer) {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(requireActivity().getLocalizedString(org.odk.collect.strings.R.string.delete_layer_confirmation_message, layerItem.name))
            .setPositiveButton(org.odk.collect.strings.R.string.delete_layer) { _, _ ->
                if (layerItem.id == stateViewModel.getCheckedLayer()) {
                    stateViewModel.onLayerChecked(null)
                }
                stateViewModel.onLayerDeleted(layerItem.id)
                sharedViewModel.onLayerDeleted(layerItem.id!!)
            }
            .setNegativeButton(org.odk.collect.strings.R.string.cancel, null)
            .create()
            .show()
    }

    private fun updateAdapter(
        layers: List<ReferenceLayer>?,
        checkedLayerId: String?,
        expandedLayerIds: List<String?>,
        adapter: OfflineMapLayersPickerAdapter
    ) {
        if (layers == null) {
            return
        }

        val newData = mutableListOf(
            CheckableReferenceLayer(
                null,
                null,
                requireContext().getLocalizedString(org.odk.collect.strings.R.string.none),
                checkedLayerId == null,
                false
            )
        )

        newData.addAll(layers.map {
            CheckableReferenceLayer(
                it.id,
                it.file,
                it.name,
                checkedLayerId == it.id,
                expandedLayerIds.contains(it.id)
            )
        })
        adapter.setData(newData)
    }
}
