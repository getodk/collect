package org.odk.collect.qrcode.mlkit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.core.TorchState
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.qrcode.BarcodeCandidate
import org.odk.collect.qrcode.BarcodeFilter
import org.odk.collect.qrcode.BarcodeFormat
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.DetectedBarcode
import org.odk.collect.qrcode.DetectedState
import org.odk.collect.qrcode.ScannerOverlay
import org.odk.collect.qrcode.calculateViewFinder
import org.odk.collect.qrcode.databinding.MlkitBarcodeScannerLayoutBinding

class MlKitBarcodeScannerViewFactory(private val scanThreshold: Int) :
    BarcodeScannerViewContainer.Factory {
    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        return MlKitBarcodeScannerView(
            activity,
            lifecycleOwner,
            qrOnly,
            useFrontCamera,
            scanThreshold
        )
    }

    companion object {
        private var ML_KIT_AVAILABLE = false

        @JvmStatic
        fun init(application: Application) {
            try {
                ModuleInstall.getClient(application)
                    .areModulesAvailable(BarcodeScanning.getClient())
                    .addOnSuccessListener {
                        if (it.areModulesAvailable()) {
                            ML_KIT_AVAILABLE = true
                        }
                    }
            } catch (e: Exception) {
                // Ignored
            }
        }

        fun isAvailable(): Boolean {
            return ML_KIT_AVAILABLE
        }
    }
}

@SuppressLint("ViewConstructor")
private class MlKitBarcodeScannerView(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val qrOnly: Boolean,
    private val useFrontCamera: Boolean,
    private val scanThreshold: Int
) : BarcodeScannerView(context) {

    private val binding =
        MlkitBarcodeScannerLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val cameraController = LifecycleCameraController(context)
    private val viewFinderRect = Rect()

    private val detectedState = mutableStateOf<DetectedState>(DetectedState.None)
    private val fullScreenViewFinderState = mutableStateOf(false)

    init {
        binding.composeView.setContextThemedContent {
            ScannerOverlay(detectedState.value, fullScreenViewFinderState.value)
        }
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        updateViewFinderSize()
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun scan(callback: (String) -> Unit) {
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
        val barcodeFilter = BarcodeFilter(viewFinderRect, scanThreshold)
        cameraController.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                executor
            ) { result: MlKitAnalyzer.Result ->
                val value = result.getValue(barcodeScanner)
                val barcodeCandidates = value?.map { it.toCandidate() } ?: emptyList()
                barcodeFilter.filter(barcodeCandidates).also {
                    detectedState.value = it

                    if (it is DetectedState.Full) {
                        val contents = processBarcode(it.barcode)
                        if (!contents.isNullOrEmpty()) {
                            cameraController.unbind()
                            callback(contents)
                        }
                    }
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

    override fun supportsFullScreenViewFinder(): Boolean {
        return true
    }

    override fun setFullScreenViewFinder(fullScreenViewFinder: Boolean) {
        fullScreenViewFinderState.value = fullScreenViewFinder
        updateViewFinderSize()
    }

    private fun updateViewFinderSize() {
        val (viewFinderOffset, viewFinderSize) = calculateViewFinder(
            this.width.toFloat(),
            this.height.toFloat(),
            fullScreenViewFinderState.value
        )

        viewFinderRect.set(
            viewFinderOffset.x.toInt(),
            viewFinderOffset.y.toInt(),
            (viewFinderOffset.x + viewFinderSize.width).toInt(),
            (viewFinderOffset.y + viewFinderSize.height).toInt()
        )
    }

    private fun processBarcode(barcode: DetectedBarcode): String? {
        return when (barcode) {
            is DetectedBarcode.Utf8 -> {
                barcode.contents
            }

            is DetectedBarcode.Bytes -> {
                if (barcode.format == BarcodeFormat.PDF417) {
                    /**
                     * Allow falling back to Latin encoding for PDF417 barcodes. This provides parity
                     * with the Zxing implementation.
                     */
                    String(barcode.bytes, Charsets.ISO_8859_1)
                } else {
                    null
                }
            }
        }
    }

    companion object {

        private fun Barcode.toCandidate(): BarcodeCandidate {
            val format = when (this.format) {
                Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF417
                else -> BarcodeFormat.OTHER
            }

            return BarcodeCandidate(this.rawBytes, this.rawValue, this.boundingBox, format)
        }
    }
}
