package org.odk.collect.android.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

open class BarcodeViewDecoder {
    open fun waitForBarcode(view: DecoratedBarcodeView): LiveData<BarcodeResult> {
        val liveData = MutableLiveData<BarcodeResult>()

        view.decodeContinuous { result -> liveData.value = result }

        return liveData
    }
}
