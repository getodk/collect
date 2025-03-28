package org.odk.collect.android.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.camera.CameraSettings
import org.odk.collect.android.databinding.ZxingBarcodeScannerLayoutBinding
import org.odk.collect.android.utilities.CodeCaptureManagerFactory
import org.odk.collect.androidshared.system.CameraUtils

@SuppressLint("ViewConstructor")
private class ZxingBarcodeScannerView(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val qrOnly: Boolean,
    private val prompt: String,
    private val useFrontCamera: Boolean
) : BarcodeScannerView(activity) {

    private val binding =
        ZxingBarcodeScannerLayoutBinding.inflate(LayoutInflater.from(activity), this, true)

    override fun decodeContinuous(callback: (String) -> Unit) {
        val supportedFormats = if (qrOnly) {
            listOf(IntentIntegrator.QR_CODE)
        } else {
            IntentIntegrator.ALL_CODE_TYPES
        }

        val captureManager = CodeCaptureManagerFactory.getCaptureManager(
            activity,
            binding.barcodeView,
            supportedFormats,
            prompt
        )

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                binding.barcodeView.resume()
                captureManager.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                binding.barcodeView.pauseAndWait()
                captureManager.onPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                captureManager.onDestroy()
            }
        })

        if (useFrontCamera) {
            val cameraSettings = CameraSettings()
            cameraSettings.requestedCameraId = CameraUtils.getFrontCameraId()
            binding.barcodeView.barcodeView.cameraSettings = cameraSettings
        }

        binding.barcodeView.decodeContinuous {
            callback(it.text)
        }
    }

    override fun setTorchOn(on: Boolean) {
        if (on) {
            binding.barcodeView.setTorchOn()
        } else {
            binding.barcodeView.setTorchOff()
        }
    }

    override fun setTorchListener(torchListener: TorchListener) {
        binding.barcodeView.setTorchListener(object : com.journeyapps.barcodescanner.DecoratedBarcodeView.TorchListener {
            override fun onTorchOn() {
                torchListener.onTorchOn()
            }

            override fun onTorchOff() {
                torchListener.onTorchOff()
            }
        })
    }
}

class ZxingBarcodeScannerViewFactory : BarcodeScannerViewContainer.Factory {
    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        prompt: String,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        return ZxingBarcodeScannerView(activity, lifecycleOwner, qrOnly, prompt, useFrontCamera)
    }
}
