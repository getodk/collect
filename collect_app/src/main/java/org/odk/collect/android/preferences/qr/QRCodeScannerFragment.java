package org.odk.collect.android.preferences.qr;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeResult;

import org.odk.collect.android.fragments.BaseCodeScannerFragment;
import org.odk.collect.android.preferences.utilities.SettingsUtils;
import org.odk.collect.android.utilities.CompressionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.DataFormatException;

public class QRCodeScannerFragment extends BaseCodeScannerFragment {
    @Override
    protected Collection<String> getSupportedCodeFormats() {
        return Collections.singletonList(IntentIntegrator.QR_CODE);
    }

    @Override
    protected void handleScanningResult(BarcodeResult result) throws IOException, DataFormatException {
        SettingsUtils.applySettings(getActivity(), CompressionUtils.decompress(result.getText()));
    }
}
