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

package org.odk.collect.android.preferences.qr;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import timber.log.Timber;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.utilities.SettingsUtils;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.ToastUtils;

import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

public class QRScannerFragment extends Fragment implements DecoratedBarcodeView.TorchListener {

    private static final String ACTION = "action";

    public enum Action {BARCODE_WIDGET, APPLY_SETTINGS};

    private CaptureManager capture;
    DecoratedBarcodeView barcodeScannerView;
    private Button switchFlashlightButton;
    private BeepManager beepManager;
    private Action action;

    public static QRScannerFragment newInstance(Action action) {
        QRScannerFragment fragment = new QRScannerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION, action);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        action = (Action) (savedInstanceState == null
                        ? getArguments().getSerializable(ACTION)
                        : savedInstanceState.getSerializable(ACTION));

        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        beepManager = new BeepManager(getActivity());
        barcodeScannerView = rootView.findViewById(R.id.barcode_view);
        switchFlashlightButton = rootView.findViewById(R.id.switch_flashlight);

        barcodeScannerView.setTorchListener(this);

        capture = new CaptureManager(getActivity(), barcodeScannerView);
        capture.initializeFromIntent(getActivity().getIntent(), savedInstanceState);
        capture.decode();

        switchFlashlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlashlight(v);
            }
        });
        new PermissionUtils().requestCameraPermission(getActivity(), new PermissionListener() {
            @Override
            public void granted() {
                barcodeScannerView.decodeContinuous(new BarcodeCallback() {
                    @Override
                    public void barcodeResult(BarcodeResult result) {
                        beepManager.playBeepSoundAndVibrate();
                        try {
                            switch (action) {
                                case APPLY_SETTINGS:
                                    SettingsUtils.applySettings(getActivity(), CompressionUtils.decompress(result.getText()));
                                    break;
                                case BARCODE_WIDGET:
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("SCAN_RESULT", result.getText());
                                    getActivity().setResult(Activity.RESULT_OK, returnIntent);
                                    getActivity().finish();
                                    break;
                            }
                        } catch (IOException | DataFormatException | IllegalArgumentException e) {
                            Timber.e(e);
                            ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
                        }
                    }

                    @Override
                    public void possibleResultPoints(List<ResultPoint> resultPoints) {

                    }
                });
            }

            @Override
            public void denied() {

            }
        });

        // if the device does not have flashlight in its camera, then remove the switch flashlight button...
        if (!hasFlash() || frontCameraUsed()) {
            switchFlashlightButton.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ACTION, action);
        capture.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeScannerView.pauseAndWait();
        capture.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeScannerView.resume();
        capture.onResume();
    }

    @Override
    public void onDestroy() {
        capture.onDestroy();
        super.onDestroy();
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean frontCameraUsed() {
        Bundle bundle = getActivity().getIntent().getExtras();
        return bundle != null && bundle.getBoolean(WidgetAppearanceUtils.FRONT);
    }

    public void switchFlashlight(View view) {
        if (getString(R.string.turn_on_flashlight).equals(switchFlashlightButton.getText())) {
            barcodeScannerView.setTorchOn();
        } else {
            barcodeScannerView.setTorchOff();
        }
    }

    @Override
    public void onTorchOn() {
        switchFlashlightButton.setText(R.string.turn_off_flashlight);
    }

    @Override
    public void onTorchOff() {
        switchFlashlightButton.setText(R.string.turn_on_flashlight);
    }
}
