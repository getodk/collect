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
import android.graphics.BitmapFactory;
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

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import io.reactivex.Observable;
import timber.log.Timber;


public class QRCodeUtils {
    public static final String QR_CODE_FILEPATH = Collect.SETTINGS + File.separator + "collect-settings.png";
    private static final int QR_CODE_SIDE_LENGTH = 400; // in pixels
    private static final String SETTINGS_MD5_FILE = ".collect-settings-hash";
    static final String MD5_CACHE_PATH = Collect.SETTINGS + File.separator + SETTINGS_MD5_FILE;

    private QRCodeUtils() {
    }

    public static String decodeFromBitmap(Bitmap bitmap) throws DataFormatException, IOException, FormatException, ChecksumException, NotFoundException {
        Map<DecodeHintType, Object> tmpHintsMap = new EnumMap<>(DecodeHintType.class);
        tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
        tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);

        Reader reader = new QRCodeMultiReader();
        Result result = reader.decode(getBinaryBitmap(bitmap), tmpHintsMap);
        return CompressionUtils.decompress(result.getText());
    }

    @NonNull
    private static BinaryBitmap getBinaryBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];

        //copy pixel data from bitmap into the array
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        return new BinaryBitmap(new HybridBinarizer(source));
    }

    public static Bitmap generateQRBitMap(String data, int sideLength) throws IOException, WriterException {
        final long time = System.currentTimeMillis();
        String compressedData = CompressionUtils.compress(data);

        // Maximum capacity for QR Codes is 4,296 characters (Alphanumeric)
        if (compressedData.length() > 4000) {
            throw new IOException(Collect.getInstance().getString(R.string.encoding_max_limit));
        }

        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(compressedData, BarcodeFormat.QR_CODE, sideLength, sideLength, hints);

        Bitmap bmp = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.RGB_565);
        for (int x = 0; x < sideLength; x++) {
            for (int y = 0; y < sideLength; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        Timber.i("QR Code generation took : %d ms", (System.currentTimeMillis() - time));
        return bmp;
    }

    public static Observable<Bitmap> getQRCodeGeneratorObservable(Collection<String> selectedPasswordKeys) {
        return Observable.create(emitter -> {
            String preferencesString = SharedPreferencesUtils.getJSONFromPreferences(selectedPasswordKeys);

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(preferencesString.getBytes());
            byte[] messageDigest = md.digest();

            boolean shouldWriteToDisk = true;
            Bitmap bitmap = null;

            // check if settings directory exists, if not then create one
            File writeDir = new File(Collect.SETTINGS);
            if (!writeDir.exists()) {
                if (!writeDir.mkdirs()) {
                    Timber.e("Error creating directory " + writeDir.getAbsolutePath());
                }
            }

            File mdCacheFile = new File(MD5_CACHE_PATH);
            if (mdCacheFile.exists()) {
                byte[] cachedMessageDigest = FileUtils.read(mdCacheFile);

                /*
                 * If the messageDigest generated from the preferences is equal to cachedMessageDigest
                 * then don't generate QRCode and read the one saved in disk
                 */
                if (Arrays.equals(messageDigest, cachedMessageDigest)) {
                    Timber.i("Loading QRCode from the disk...");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    bitmap = FileUtils.getBitmap(QR_CODE_FILEPATH, options);
                    shouldWriteToDisk = false;
                }
            }

            // If the file is not found in the disk or md5Hash not matched
            if (bitmap == null) {
                Timber.i("Generating QRCode...");
                bitmap = generateQRBitMap(preferencesString, QR_CODE_SIDE_LENGTH);
                shouldWriteToDisk = true;
            }

            if (bitmap != null) {
                // Send the QRCode to the observer
                emitter.onNext(bitmap);

                // Save the QRCode to disk
                if (shouldWriteToDisk) {
                    Timber.i("Saving QR Code to disk... : " + QR_CODE_FILEPATH);
                    FileUtils.saveBitmapToFile(bitmap, QR_CODE_FILEPATH);

                    FileUtils.write(mdCacheFile, messageDigest);
                    Timber.i("Updated %s file contents", SETTINGS_MD5_FILE);
                }

                // Send the task completion event
                emitter.onComplete();
            }
        });
    }
}
