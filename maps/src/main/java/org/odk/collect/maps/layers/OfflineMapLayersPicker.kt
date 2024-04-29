package org.odk.collect.maps.layers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBinding
import org.odk.collect.shared.TempFiles

class OfflineMapLayersPicker : BottomSheetDialogFragment() {
    private lateinit var offlineMapLayersPickerBinding: OfflineMapLayersPickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        offlineMapLayersPickerBinding = OfflineMapLayersPickerBinding.inflate(inflater)

        val layers = listOf(
            ReferenceLayer("1", TempFiles.createTempFile()),
            ReferenceLayer("2", TempFiles.createTempFile()),
            ReferenceLayer("3", TempFiles.createTempFile()),
            ReferenceLayer("4", TempFiles.createTempFile()),
            ReferenceLayer("5", TempFiles.createTempFile()),
            ReferenceLayer("6", TempFiles.createTempFile()),
            ReferenceLayer("7", TempFiles.createTempFile()),
            ReferenceLayer("8", TempFiles.createTempFile()),
            ReferenceLayer("9", TempFiles.createTempFile()),
            ReferenceLayer("10", TempFiles.createTempFile()),
            ReferenceLayer("11", TempFiles.createTempFile()),
            ReferenceLayer("12", TempFiles.createTempFile()),
            ReferenceLayer("13", TempFiles.createTempFile()),
            ReferenceLayer("14", TempFiles.createTempFile()),
            ReferenceLayer("15", TempFiles.createTempFile()),
            ReferenceLayer("16", TempFiles.createTempFile()),
            ReferenceLayer("17", TempFiles.createTempFile()),
            ReferenceLayer("18", TempFiles.createTempFile()),
            ReferenceLayer("19", TempFiles.createTempFile())
        )
        val offlineMapLayersAdapter = OfflineMapLayersAdapter(layers, null)
        offlineMapLayersPickerBinding.layers.setAdapter(offlineMapLayersAdapter)

        offlineMapLayersPickerBinding.cancel.setOnClickListener {
            dismiss()
        }
        offlineMapLayersPickerBinding.save.setOnClickListener {
            dismiss()
        }
        return offlineMapLayersPickerBinding.root
    }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    companion object {
        const val TAG = "OfflineMapLayersPicker"
    }
}
