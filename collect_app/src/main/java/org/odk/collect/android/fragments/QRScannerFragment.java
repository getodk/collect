/* Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.fragments;
import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import timber.log.Timber;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.ActionListener;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceSaver;
import org.odk.collect.android.preferences.QRCodeTabs;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;

public class QRScannerFragment extends Fragment {

    DecoratedBarcodeView barcodeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        barcodeView = rootView.findViewById(R.id.barcode_view);

        new PermissionUtils().requestCameraPermission(getActivity(), new PermissionListener() {
            @Override
            public void granted() {
                barcodeView.decodeContinuous(new BarcodeCallback() {
                    @Override
                    public void barcodeResult(BarcodeResult result) {
                        beepSound();
                    }

                    @Override
                    public void possibleResultPoints(List<ResultPoint> resultPoints) {

                    }
                });
            }
            public void barcodeResult(BarcodeResult result) {
                beepSound();
                try {
                    QRCodeTabs.applySettings(getActivity(), CompressionUtils.decompress(result.getText()));
                } catch (IOException | DataFormatException | IllegalArgumentException e) {
                    Timber.e(e);
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
                }
            }

            @Override
            public void denied() {

            }
        });

        return rootView;
    }

    protected void beepSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pauseAndWait();
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }



}
