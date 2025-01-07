package org.odk.collect.maps.layers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.databinding.OfflineMapLayersImporterBinding
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R
import org.odk.collect.strings.localization.getLocalizedQuantityString
import org.odk.collect.strings.localization.getLocalizedString

class OfflineMapLayersImporterDialogFragment(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider
) : MaterialFullScreenDialogFragment() {
    val viewModel: OfflineMapLayersViewModel by activityViewModels {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = OfflineMapLayersImporterBinding.inflate(inflater)
        binding.toolbarLayout.toolbar.setTitle(R.string.add_layer)

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addLayerButton.setOnClickListener {
            viewModel.importNewLayers(binding.allProjectsOption.isChecked)
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = OfflineMapLayersImporterBinding.bind(view)

        viewModel.trackableWorker.isWorking.observe(this) { isLoading ->
            if (isLoading) {
                binding.addLayerButton.isEnabled = false
                binding.layers.visibility = View.GONE
                binding.progressIndicator.visibility = View.VISIBLE
            } else {
                binding.addLayerButton.isEnabled = true
                binding.layers.visibility = View.VISIBLE
                binding.progressIndicator.visibility = View.GONE
            }
        }

        viewModel.layersToImport.observe(this) { layersToImport ->
            val adapter = OfflineMapLayersImporterAdapter(layersToImport.value.layers)
            binding.layers.setAdapter(adapter)

            if (!layersToImport.isConsumed()) {
                layersToImport.consume()

                if (layersToImport.value.numberOfSelectedLayers == layersToImport.value.numberOfUnsupportedLayers) {
                    dismiss()
                    showNoSupportedLayersWarning(layersToImport.value.numberOfUnsupportedLayers)
                } else if (layersToImport.value.numberOfUnsupportedLayers > 0) {
                    showSomeUnsupportedLayersWarning(layersToImport.value.numberOfUnsupportedLayers)
                }
            }
        }
    }

    override fun onCloseClicked() = Unit

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar {
        return OfflineMapLayersImporterBinding.bind(requireView()).toolbarLayout.toolbar
    }

    private fun showNoSupportedLayersWarning(numberOfLayers: Int) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(
                requireActivity().getLocalizedQuantityString(
                    R.plurals.non_mbtiles_files_selected_title,
                    numberOfLayers,
                    numberOfLayers
                )
            )
            .setMessage(requireActivity().getLocalizedString(R.string.all_non_mbtiles_files_selected_message))
            .setPositiveButton(R.string.ok, null)
            .create()
            .show()
    }

    private fun showSomeUnsupportedLayersWarning(numberOfLayers: Int) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(
                requireActivity().getLocalizedQuantityString(
                    R.plurals.non_mbtiles_files_selected_title,
                    numberOfLayers,
                    numberOfLayers
                )
            )
            .setMessage(requireActivity().getLocalizedString(R.string.some_non_mbtiles_files_selected_message))
            .setPositiveButton(R.string.ok, null)
            .create()
            .show()
    }
}
