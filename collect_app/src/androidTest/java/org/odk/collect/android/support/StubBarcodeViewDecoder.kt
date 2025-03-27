package org.odk.collect.android.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.fragments.BarcodeScannerView
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.androidshared.utils.CompressionUtils
import java.io.IOException

class StubBarcodeViewDecoder : BarcodeViewDecoder() {
    var liveData = MutableLiveData<String>()

    override fun waitForBarcode(view: BarcodeScannerView): LiveData<String> {
        return liveData
    }

    fun scan(settings: String?) {
        try {
            liveData.postValue(CompressionUtils.compress(settings))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
