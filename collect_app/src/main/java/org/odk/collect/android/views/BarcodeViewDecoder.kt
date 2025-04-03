package org.odk.collect.android.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.qrcode.BarcodeScannerView

object BarcodeViewDecoder {

    @JvmStatic
    fun waitForBarcode(view: BarcodeScannerView): LiveData<String> {
        return MutableLiveData<String>().also {
            view.decodeContinuous { result -> it.value = result }
        }
    }
}
