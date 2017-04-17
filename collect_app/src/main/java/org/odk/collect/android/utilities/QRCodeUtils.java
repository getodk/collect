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

package org.odk.collect.android.utilities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.utilities.SharedPreferencesUtils.getJSONFromPreferences;

/**
 * Created by shobhit on 13/4/17.
 */

public class QRCodeUtils {

    public static String decodeFromBitmap(Bitmap bitmap) {
        BinaryBitmap binaryBitmap = getBinaryBitmap(bitmap);

        Map<DecodeHintType, Object> tmpHintsMap = new EnumMap<DecodeHintType, Object>(
                DecodeHintType.class);

        tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
        tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);

        Reader reader = new QRCodeMultiReader();
        try {
            Result result = reader.decode(binaryBitmap, tmpHintsMap);
            return result.getText();
        } catch (FormatException | NotFoundException | ChecksumException e) {
            Timber.i(e);
            ToastUtils.showLongToast("QR Code not found in the selected image");
        }
        return null;
    }

    @NonNull
    private static BinaryBitmap getBinaryBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];

        //copy pixel data from bitmap into the array
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        return new BinaryBitmap(new HybridBinarizer(source));
    }

    public static File saveBitmapToCache(Bitmap qrCode) throws IOException {
        //Save the bitmap to a file
        File cache = Collect.getInstance().getApplicationContext().getExternalCacheDir();
        File shareFile = new File(cache, "shareImage.jpeg");
        FileOutputStream out = new FileOutputStream(shareFile);
        qrCode.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();
        return shareFile;
    }

    public static Bitmap generateQRBitMap() {
        String content;
        try {
            content = getJSONFromPreferences();
            String compressedData = CompressionUtils.compress(content);

            //Maximum capacity for QR Codes is 4,296 characters (Alphanumeric)
            if (compressedData.length() > 4000) {
                ToastUtils.showLongToast(Collect.getInstance().getString(R.string.encoding_max_limit));
                Timber.e(Collect.getInstance().getString(R.string.encoding_max_limit));
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
}
