package org.odk.collect.android.fragments

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.mlkit.vision.barcode.BarcodeScanning

class PlayServicesMlKitBarcodeScannerViewFactory : BarcodeScannerViewContainer.Factory {

    private val mlKitBarcodeScannerViewFactory = MlKitBarcodeScannerViewFactory()
    private val zxingBarcodeScannerViewFactory = ZxingBarcodeScannerViewFactory()

    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        prompt: String,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        if (ML_KIT_AVAILABLE) {
            return mlKitBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                prompt,
                useFrontCamera
            )
        } else {
            ModuleInstall.getClient(activity)
                .areModulesAvailable(BarcodeScanning.getClient())
                .addOnSuccessListener {
                    if (it.areModulesAvailable()) {
                        ML_KIT_AVAILABLE = true
                    }
                }

            return zxingBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                prompt,
                useFrontCamera
            )
        }
    }

    companion object {
        private var ML_KIT_AVAILABLE = false
    }
}
