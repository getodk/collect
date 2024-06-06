package org.odk.collect.maps.layers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.databinding.OfflineMapLayersImporterBinding
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.settings.SettingsProvider

class OfflineMapLayersImporter(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider
) : MaterialFullScreenDialogFragment() {
    val viewModel: OfflineMapLayersViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineMapLayersViewModel(referenceLayerRepository, scheduler, settingsProvider, requireContext().contentResolver) as T
            }
        }
    }

    private lateinit var binding: OfflineMapLayersImporterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OfflineMapLayersImporterBinding.inflate(inflater)

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

        viewModel.isLoading.observe(this) { isLoading ->
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
            val adapter = OfflineMapLayersImporterAdapter(layersToImport)
            binding.layers.setAdapter(adapter)
        }
    }

    override fun onCloseClicked() = Unit

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }
}
