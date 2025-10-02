package org.odk.collect.qrcode.mlkit

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.zxing.ZxingBarcodeScannerViewFactory

class PlayServicesFallbackBarcodeScannerViewFactory(mlkitScanThreshold: Int) : BarcodeScannerViewContainer.Factory {

    private val mlKitBarcodeScannerViewFactory = MlKitBarcodeScannerViewFactory(mlkitScanThreshold)
    private val zxingBarcodeScannerViewFactory = ZxingBarcodeScannerViewFactory()

    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        if (MlKitBarcodeScannerViewFactory.isAvailable()) {
            return mlKitBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                useFrontCamera
            )
        } else {
            return zxingBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                useFrontCamera
            )
        }
    }
}
