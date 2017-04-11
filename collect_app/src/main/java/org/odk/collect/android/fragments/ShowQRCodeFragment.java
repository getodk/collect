/*
 * Copyright (C) 2017 Shobhit
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

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_GOOGLE_SHEETS_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SERVER_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;


/**
 * Created by shobhit on 6/4/17.
 */

public class ShowQRCodeFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences settings;
    private ProgressDialog progressDialog;
    private int mProgress = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_qrcode_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize();

        Button scan = (Button) view.findViewById(R.id.btnScan);
        scan.setOnClickListener(this);

        Bitmap qrCode = generateQRBitMap();
        if (qrCode != null) {
            ImageView qrImageView = (ImageView) view.findViewById(R.id.qr_iv);
            qrImageView.setImageBitmap(qrCode);
        }

        progressDialog.dismiss();
    }

    private void initialize() {
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Generating QRCode...");
        progressDialog.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Horizontal);
        progressDialog.setProgress(mProgress);
        progressDialog.show();
    }

    private Bitmap generateQRBitMap() {
        String content;
        try {
            content = getServerSettings();
            String compressedData = TextUtils.compress(content);

            //Maximum capacity for QR Codes is 4,296 characters (Alphanumeric)
            if (compressedData.length() > 4000) {
                ToastUtils.showLongToast(getString(R.string.encoding_max_limit));
                Timber.e(getString(R.string.encoding_max_limit));
                return null;
            }

            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter
                    .encode(compressedData, BarcodeFormat.QR_CODE, 512, 512, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            progressDialog.setMax(width * height);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    progressDialog.setProgress(mProgress++);
                }
            }
            return bmp;
        } catch (WriterException | IOException | JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                IntentIntegrator integrator = IntentIntegrator.forFragment(this);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.initiateScan();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // request was canceled...
                ToastUtils.showShortToast("Scanning Cancelled");
            } else {
                try {
                    String decompressedData = TextUtils.decompress(result.getContents());
                    JSONObject jsonObject = new JSONObject(decompressedData);
                    applySettings(jsonObject);
                    ToastUtils.showLongToast(getString(R.string.successfully_imported_settings));
                } catch (JSONException | IOException | DataFormatException e) {
                    Timber.e(e);
                }
            }
        }
    }

    private void applySettings(JSONObject jsonObject) throws JSONException {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_PROTOCOL, jsonObject.getString(KEY_PROTOCOL));
        editor.putString(KEY_SERVER_URL, jsonObject.getString(KEY_SERVER_URL));
        editor.putString(KEY_GOOGLE_SHEETS_URL, jsonObject.getString(KEY_GOOGLE_SHEETS_URL));
        editor.putString(KEY_FORMLIST_URL, jsonObject.getString(KEY_FORMLIST_URL));
        editor.putString(KEY_SUBMISSION_URL, jsonObject.getString(KEY_SUBMISSION_URL));
        editor.putString(KEY_USERNAME, jsonObject.getString(KEY_USERNAME));
        editor.putString(KEY_PASSWORD, jsonObject.getString(KEY_PASSWORD));
        editor.apply();
    }

    private String getServerSettings() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_PROTOCOL, settings.getString(KEY_PROTOCOL, null));
        jsonObject.put(KEY_SERVER_URL, settings.getString(KEY_SERVER_URL,
                getActivity().getString(R.string.default_server_url)));
        jsonObject.put(KEY_GOOGLE_SHEETS_URL, settings.getString(KEY_GOOGLE_SHEETS_URL,
                getActivity().getString(R.string.default_google_sheets_url)));
        jsonObject.put(KEY_FORMLIST_URL, settings.getString(PreferenceKeys.KEY_FORMLIST_URL,
                getActivity().getString(R.string.default_odk_formlist)));
        jsonObject.put(KEY_SUBMISSION_URL, settings.getString(PreferenceKeys.KEY_SUBMISSION_URL,
                getActivity().getString(R.string.default_odk_submission)));
        jsonObject.put(KEY_USERNAME, settings.getString(PreferenceKeys.KEY_USERNAME, ""));
        jsonObject.put(KEY_PASSWORD, settings.getString(PreferenceKeys.KEY_PASSWORD, ""));
        return jsonObject.toString();
    }
}
