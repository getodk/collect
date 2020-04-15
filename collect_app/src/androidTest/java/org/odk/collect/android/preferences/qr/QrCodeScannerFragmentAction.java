package org.odk.collect.android.preferences.qr;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import static org.junit.Assert.assertNotNull;


public class QrCodeScannerFragmentAction implements FragmentScenario.FragmentAction<QRScannerFragment> {
    @Override
    public void perform(@NonNull QRScannerFragment fragment) {
        assertNotNull(fragment.barcodeScannerView);
    }
}
