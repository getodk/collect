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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.QRCodeListener;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.utilities.SharedPreferencesUtils.getJSONFromPreferences;

/**
 * Created by shobhit on 12/4/17.
 */

public class GenerateQRCode extends AsyncTask<Void, Void, Bitmap> {


    private final QRCodeListener listener;
    private final Context context;

    public GenerateQRCode(QRCodeListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    private Bitmap generateQRBitMap() {
        String content;
        try {
            content = getJSONFromPreferences();
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
