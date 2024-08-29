package org.odk.collect.maps.layers

import android.content.Context
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
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.livedata.LiveDataUtils
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.addOnClickListener
import org.odk.collect.async.Scheduler
import org.odk.collect.lists.selects.MultiSelectViewModel
import org.odk.collect.lists.selects.SelectItem
import org.odk.collect.lists.selects.SingleSelectViewModel
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBinding
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.webpage.ExternalWebPageHelper

class OfflineMapLayersPickerBottomSheetDialogFragment(
    registry: ActivityResultRegistry,
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider,
    private val externalWebPageHelper: ExternalWebPageHelper
) : BottomSheetDialogFragment(),
    OfflineMapLayersPickerAdapter.OfflineMapLayersPickerAdapterInterface {

    private val sharedViewModel: OfflineMapLayersViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineMapLayersViewModel(
                    referenceLayerRepository,
                    scheduler,
                    settingsProvider
                ) as T
            }
        }
    }
    private val expandedStateViewModel: MultiSelectViewModel<*> by viewModels {
        MultiSelectViewModel.Factory<Any>()
    }

    private val checkedStateViewModel: SingleSelectViewModel by viewModels {
        viewModelFactory {
            addInitializer(SingleSelectViewModel::class) {
                SingleSelectViewModel(
                    settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER),
                    sharedViewModel.existingLayers.map {
                        it.map { layer ->
                            SelectItem(layer.id, layer)
                        }
                    }
                )
            }
        }
    }

    private val getLayers = registerForActivityResult(ActivityResultContracts.GetMultipleContents(), registry) { uris ->
        if (uris.isNotEmpty()) {
            sharedViewModel.loadLayersToImport(uris, requireContext())
            DialogFragmentUtils.showIfNotShowing(
                OfflineMapLayersImporterDialogFragment::class.java,
                childFragmentManager
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedViewModel.loadExistingLayers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(OfflineMapLayersImporterDialogFragment::class) {
                OfflineMapLayersImporterDialogFragment(referenceLayerRepository, scheduler, settingsProvider)
            }
            .build()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = OfflineMapLayersPickerBinding.inflate(inflater)

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
            sharedViewModel.saveCheckedLayer(checkedStateViewModel.getSelected().value)
            dismiss()
        }

        if (sharedViewModel.layersToImport.value?.value == null) {
            DialogFragmentUtils.dismissDialog(
                OfflineMapLayersImporterDialogFragment::class.java,
                childFragmentManager
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = OfflineMapLayersPickerBinding.bind(view)

        sharedViewModel.trackableWorker.isWorking.observe(this) { isLoading ->
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
            checkedStateViewModel.getSelected(),
            expandedStateViewModel.getSelected()
        ).observe(this) { (layers, checkedLayerId, expandedLayerIds) ->
            updateAdapter(layers, checkedLayerId, expandedLayerIds.toList(), adapter)
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
        if (layerId != null) {
            checkedStateViewModel.select(layerId)
        } else {
            checkedStateViewModel.clear()
        }
    }

    override fun onLayerToggled(layerId: String) {
        expandedStateViewModel.toggle(layerId)
    }

    override fun onDeleteLayer(layerItem: CheckableReferenceLayer) {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(
                requireActivity().getLocalizedString(
                    org.odk.collect.strings.R.string.delete_layer_confirmation_message,
                    layerItem.name
                )
            )
            .setPositiveButton(org.odk.collect.strings.R.string.delete_layer) { _, _ ->
                sharedViewModel.deleteLayer(layerItem.id!!)
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

        newData.addAll(
            layers.map {
                CheckableReferenceLayer(
                    it.id,
                    it.file,
                    it.name,
                    checkedLayerId == it.id,
                    expandedLayerIds.contains(it.id)
                )
            }
        )
        adapter.setData(newData)
    }
}
