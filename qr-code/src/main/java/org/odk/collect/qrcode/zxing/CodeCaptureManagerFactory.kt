package org.odk.collect.qrcode.zxing

import android.app.Activity
import android.content.Intent
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

internal object CodeCaptureManagerFactory {

    fun getCaptureManager(
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
