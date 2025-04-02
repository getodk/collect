package org.odk.collect.android.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.core.TorchState
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import org.odk.collect.android.databinding.MlkitBarcodeScannerLayoutBinding

@SuppressLint("ViewConstructor")
private class MlKitBarcodeScannerView(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val qrOnly: Boolean,
    private val useFrontCamera: Boolean,
    prompt: String
) : BarcodeScannerView(context) {

    private val binding =
        MlkitBarcodeScannerLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val cameraController = LifecycleCameraController(context)

    init {
        binding.prompt.text = prompt
    }

    override fun decodeContinuous(callback: (String) -> Unit) {
        if (useFrontCamera) {
            cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }

        cameraController.bindToLifecycle(lifecycleOwner)
        binding.preview.setController(cameraController)

        val format = if (qrOnly) {
            Barcode.FORMAT_QR_CODE
        } else {
            Barcode.FORMAT_ALL_FORMATS
        }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(format)
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)

        val executor = ContextCompat.getMainExecutor(context)
        cameraController.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                executor
            ) { result: MlKitAnalyzer.Result ->
                val value = result.getValue(barcodeScanner)
                if (value!!.isNotEmpty()) {
                    callback(value.first().rawValue!!)
                    cameraController.unbind()
                }
            }
        )
    }

    override fun setTorchOn(on: Boolean) {
        cameraController.enableTorch(on)
    }

    override fun setTorchListener(torchListener: TorchListener) {
        cameraController.torchState.observe(lifecycleOwner) {
            if (it == TorchState.ON) {
                torchListener.onTorchOn()
            } else if (it == TorchState.OFF) {
                torchListener.onTorchOff()
            }
        }
    }
}

class MlKitBarcodeScannerViewFactory : BarcodeScannerViewContainer.Factory {
    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        prompt: String,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        return MlKitBarcodeScannerView(activity, lifecycleOwner, qrOnly, useFrontCamera, prompt)
    }

    companion object {
        private var ML_KIT_AVAILABLE = false

        @JvmStatic
        fun init(application: Application) {
            ModuleInstall.getClient(application)
                .areModulesAvailable(BarcodeScanning.getClient())
                .addOnSuccessListener {
                    if (it.areModulesAvailable()) {
                        ML_KIT_AVAILABLE = true
                    }
                }
        }

        fun isAvailable(): Boolean {
            return ML_KIT_AVAILABLE
        }
    }
}
