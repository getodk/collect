package org.odk.collect.qrcode.zxing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.camera.CameraSettings
import org.odk.collect.androidshared.system.CameraUtils
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.databinding.ZxingBarcodeScannerLayoutBinding

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

    override fun scan(callback: (String) -> Unit) {
        val supportedFormats = if (qrOnly) {
            listOf(IntentIntegrator.QR_CODE)
        } else {
            IntentIntegrator.ALL_CODE_TYPES
        }

        val captureManager = getCaptureManager(
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

        binding.barcodeView.decodeSingle {
            captureManager.onDestroy()
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

    private fun getCaptureManager(
        activity: Activity,
        barcodeView: DecoratedBarcodeView,
        supportedFormats: Collection<String>?,
        prompt: String = ""
    ): CaptureManager {
        val captureManager = CaptureManager(activity, barcodeView)
        captureManager.initializeFromIntent(getIntent(activity, supportedFormats, prompt), null)
        captureManager.decode()
        return captureManager
    }

    private fun getIntent(activity: Activity, supportedFormats: Collection<String>?, prompt: String = ""): Intent {
        return IntentIntegrator(activity)
            .setDesiredBarcodeFormats(supportedFormats)
            .setPrompt(prompt)
            .setOrientationLocked(false) // Let UI control orientation lock
            .addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN)
            .createScanIntent()
    }
}
