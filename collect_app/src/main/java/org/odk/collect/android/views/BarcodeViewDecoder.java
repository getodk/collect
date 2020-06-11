package org.odk.collect.android.views;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class BarcodeViewDecoder {

    public LiveData<BarcodeResult> waitForBarcode(DecoratedBarcodeView view) {
        MutableLiveData<BarcodeResult> liveData = new MutableLiveData<>();

        view.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                liveData.setValue(result);
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }
        });

        return liveData;
    }
}
