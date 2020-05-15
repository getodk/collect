package org.odk.collect.android.fragments;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.util.Collection;

public class BarcodeWidgetScannerFragment extends BaseCodeScannerFragment {
    @Override
    protected Collection<String> getSupportedCodeFormats() {
        return IntentIntegrator.ALL_CODE_TYPES;
    }

    @Override
    protected void handleScanningResult(BarcodeResult result) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("SCAN_RESULT", result.getText());
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }
}
