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

import static org.odk.collect.android.injection.DaggerUtils.getComponent;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.zxing.client.android.BeepManager;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.async.Scheduler;
import org.odk.collect.qrcode.BarcodeScannerViewContainer;
import org.odk.collect.qrcode.FlashlightToggleView;

import javax.inject.Inject;

public abstract class BarCodeScannerFragment extends Fragment {

    private BarcodeScannerViewContainer barcodeScannerViewContainer;

    private BeepManager beepManager;

    @Inject
    BarcodeScannerViewContainer.Factory barcodeScannerViewFactory;

    @Inject
    Scheduler scheduler;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getComponent(context).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        beepManager = new BeepManager(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        barcodeScannerViewContainer = rootView.findViewById(R.id.barcode_view);
        barcodeScannerViewContainer.setup(barcodeScannerViewFactory, requireActivity(), getViewLifecycleOwner(), isQrOnly(), getContext().getString(org.odk.collect.strings.R.string.barcode_scanner_prompt), frontCameraUsed());

        FlashlightToggleView flashlightToggleView = rootView.<FlashlightToggleView>findViewById(R.id.switch_flashlight);
        flashlightToggleView.setup(barcodeScannerViewContainer.getBarcodeScannerView());
        // if the device does not have flashlight in its camera, then remove the switch flashlight button...
        if (!hasFlash() || frontCameraUsed()) {
            flashlightToggleView.setVisibility(View.GONE);
        }

        barcodeScannerViewContainer.getBarcodeScannerView().getLatestBarcode().observe(getViewLifecycleOwner(), result -> {
            try {
                beepManager.playBeepSoundAndVibrate();
            } catch (Exception ignored) {
                // ignored
            }
            handleScanningResult(result);
        });

        barcodeScannerViewContainer.getBarcodeScannerView().start();

        return rootView;
    }

    private boolean hasFlash() {
        return getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean frontCameraUsed() {
        Bundle bundle = getActivity().getIntent().getExtras();
        return bundle != null && bundle.getBoolean(Appearances.FRONT);
    }

    protected void restartScanning() {
        scheduler.immediate(true, 2000L, () -> barcodeScannerViewContainer.getBarcodeScannerView().start());
    }

    protected abstract boolean isQrOnly();

    protected abstract void handleScanningResult(String result);
}
