package org.odk.collect.android.configure.qr;

import android.content.Context;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeResult;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.fragments.BarCodeScannerFragment;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;
import static org.odk.collect.android.utilities.CompressionUtils.decompress;

public class QRCodeScannerFragment extends BarCodeScannerFragment {

    @Inject
    SettingsImporter settingsImporter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    protected void handleScanningResult(BarcodeResult result) throws IOException, DataFormatException {
        boolean importSuccess = settingsImporter.fromJSON(decompress(result.getText()));

        if (importSuccess) {
            ToastUtils.showLongToast(Collect.getInstance().getString(R.string.successfully_imported_settings));
            startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity.class);
        } else {
            ToastUtils.showLongToast(Collect.getInstance().getString(R.string.invalid_qrcode));
        }
    }

    @Override
    protected Collection<String> getSupportedCodeFormats() {
        return Collections.singletonList(IntentIntegrator.QR_CODE);
    }
}
