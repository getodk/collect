package org.odk.collect.android.fragments;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeResult;

import org.odk.collect.externalapp.ExternalAppUtils;

import java.util.Collection;

public class BarcodeWidgetScannerFragment extends BarCodeScannerFragment {
    @Override
    protected Collection<String> getSupportedCodeFormats() {
        return IntentIntegrator.ALL_CODE_TYPES;
    }

    @Override
    protected void handleScanningResult(BarcodeResult result) {
        ExternalAppUtils.returnSingleValue(getActivity(), result.getText());
    }
}
