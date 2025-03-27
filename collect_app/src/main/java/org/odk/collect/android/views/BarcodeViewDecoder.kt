package org.odk.collect.android.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.fragments.BarcodeScannerView

open class BarcodeViewDecoder {
    open fun waitForBarcode(view: BarcodeScannerView): LiveData<String> {
        return MutableLiveData<String>().also {
            view.decodeContinuous { result -> it.value = result }
        }
    }
}
