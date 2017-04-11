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

package org.odk.collect.android.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.QRCodeListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_GOOGLE_SHEETS_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SERVER_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;

/**
 * Created by shobhit on 12/4/17.
 */

public class GenerateQRCode extends AsyncTask<Void, Void, Bitmap> {


    private final QRCodeListener listener;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final ImageView imageView;

    public GenerateQRCode(QRCodeListener listener, Context context, ImageView imageView) {
        this.listener = listener;
        this.context = context;
        this.imageView = imageView;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private Bitmap generateQRBitMap() {
        String content;
        try {
            content = getPreferencesJSON();
            String compressedData = CompressionUtils.compress(content);

            //Maximum capacity for QR Codes is 4,296 characters (Alphanumeric)
            if (compressedData.length() > 4000) {
                ToastUtils.showLongToast(context.getString(R.string.encoding_max_limit));
                Timber.e(context.getString(R.string.encoding_max_limit));
                return null;
            }

            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter
                    .encode(compressedData, BarcodeFormat.QR_CODE, 400, 400, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException | IOException | JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    private String getPreferencesJSON() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_PROTOCOL, sharedPreferences.getString(KEY_PROTOCOL, null));
        jsonObject.put(KEY_SERVER_URL, sharedPreferences.getString(KEY_SERVER_URL,
                context.getString(R.string.default_server_url)));
        jsonObject.put(KEY_GOOGLE_SHEETS_URL, sharedPreferences.getString(KEY_GOOGLE_SHEETS_URL,
                context.getString(R.string.default_google_sheets_url)));
        jsonObject.put(KEY_FORMLIST_URL, sharedPreferences.getString(PreferenceKeys.KEY_FORMLIST_URL,
                context.getString(R.string.default_odk_formlist)));
        jsonObject.put(KEY_SUBMISSION_URL, sharedPreferences.getString(PreferenceKeys.KEY_SUBMISSION_URL,
                context.getString(R.string.default_odk_submission)));
        jsonObject.put(KEY_USERNAME, sharedPreferences.getString(PreferenceKeys.KEY_USERNAME, ""));
        jsonObject.put(KEY_PASSWORD, sharedPreferences.getString(PreferenceKeys.KEY_PASSWORD, ""));
        return jsonObject.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.preExecute();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        listener.bitmapGenerated(bitmap);
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        return generateQRBitMap();
    }
}
