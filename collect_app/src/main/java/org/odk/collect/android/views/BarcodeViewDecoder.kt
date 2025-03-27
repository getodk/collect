package org.odk.collect.android.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.journeyapps.barcodescanner.DecoratedBarcodeView

open class BarcodeViewDecoder {
    open fun waitForBarcode(view: DecoratedBarcodeView): LiveData<String> {
        return MutableLiveData<String>().also {
            view.decodeContinuous { result -> it.value = result.text }
        }
    }
}
