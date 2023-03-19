package org.odk.collect.android.fragments

import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import org.odk.collect.externalapp.ExternalAppUtils.returnSingleValue

class BarcodeWidgetScannerFragment : BarCodeScannerFragment() {
    override fun getSupportedCodeFormats(): Collection<String>? {
        return IntentIntegrator.ALL_CODE_TYPES
    }

    override fun handleScanningResult(result: BarcodeResult) {
        returnSingleValue(requireActivity(), result.text)
    }
}
