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

package org.odk.collect.android.configure.qr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.CompressionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

public class QRCodeUtils implements QRCodeDecoder {

    private static final int QR_CODE_SIDE_LENGTH = 400; // in pixels

    public Bitmap encode(String data) throws IOException, WriterException {
        String compressedData = CompressionUtils.compress(data);

        // Maximum capacity for QR Codes is 4,296 characters (Alphanumeric)
        if (compressedData.length() > 4000) {
            throw new IOException(getLocalizedString(Collect.getInstance(), R.string.encoding_max_limit));
        }

        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(compressedData, BarcodeFormat.QR_CODE, QR_CODE_SIDE_LENGTH, QR_CODE_SIDE_LENGTH, hints);

        Bitmap bmp = Bitmap.createBitmap(QR_CODE_SIDE_LENGTH, QR_CODE_SIDE_LENGTH, Bitmap.Config.RGB_565);
        for (int x = 0; x < QR_CODE_SIDE_LENGTH; x++) {
            for (int y = 0; y < QR_CODE_SIDE_LENGTH; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bmp;
    }

    @Override
    public String decode(InputStream inputStream) throws InvalidException, NotFoundException {
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return decode(bitmap);
    }

    private String decode(Bitmap bitmap) throws InvalidException, NotFoundException {
        Map<DecodeHintType, Object> tmpHintsMap = new EnumMap<>(DecodeHintType.class);
        tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
        tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);



        try {
            QRCodeMultiReader reader = new QRCodeMultiReader();
            Result result = reader.decode(getBinaryBitmap(bitmap), tmpHintsMap);
            return CompressionUtils.decompress(result.getText());
        } catch (DataFormatException | IOException | IllegalArgumentException e) {
            throw new InvalidException();
        } catch (FormatException | com.google.zxing.NotFoundException | ChecksumException e) {
            throw new NotFoundException();
        }
    }

    @NonNull
    private static BinaryBitmap getBinaryBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];

        //copy pixel data from bitmap into the array
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        return new BinaryBitmap(new HybridBinarizer(source));
    }
}
