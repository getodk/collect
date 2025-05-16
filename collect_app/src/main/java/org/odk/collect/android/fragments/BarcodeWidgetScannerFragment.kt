package org.odk.collect.android.fragments

import org.odk.collect.externalapp.ExternalAppUtils.returnSingleValue

class BarcodeWidgetScannerFragment : BarCodeScannerFragment() {
    override fun isQrOnly(): Boolean {
        return false
    }

    override fun handleScanningResult(result: String) {
        returnSingleValue(requireActivity(), result)
    }
}
