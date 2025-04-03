package org.odk.collect.qrcode

import android.app.Activity
import androidx.lifecycle.LifecycleOwner

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
        if (MlKitBarcodeScannerViewFactory.isAvailable()) {
            return mlKitBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                prompt,
                useFrontCamera
            )
        } else {
            return zxingBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                prompt,
                useFrontCamera
            )
        }
    }
}
