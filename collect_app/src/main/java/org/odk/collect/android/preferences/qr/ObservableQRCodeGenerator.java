package org.odk.collect.android.preferences.qr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.SharedPreferencesUtils;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import timber.log.Timber;

public class ObservableQRCodeGenerator implements QRCodeGenerator {
    private static final int QR_CODE_SIDE_LENGTH = 400; // in pixels
    private static final String SETTINGS_MD5_FILE = ".collect-settings-hash";

    @Override
    public Bitmap generateQRBitMap(String data, int sideLength) throws IOException, WriterException {
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
        Timber.i("QR Code generation took : %d ms", System.currentTimeMillis() - time);
        return bmp;
    }

    @Override
    public Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys) {
        return Observable.create(emitter -> {
            String preferencesString = SharedPreferencesUtils.getJSONFromPreferences(selectedPasswordKeys);

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(preferencesString.getBytes());
            byte[] messageDigest = md.digest();

            boolean shouldWriteToDisk = true;
            Bitmap bitmap = null;

            // check if settings directory exists, if not then create one
            File writeDir = new File(new StoragePathProvider().getDirPath(StorageSubdirectory.SETTINGS));
            if (!writeDir.exists()) {
                if (!writeDir.mkdirs()) {
                    Timber.e("Error creating directory " + writeDir.getAbsolutePath());
                }
            }

            File mdCacheFile = new File(getMd5CachePath());
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
                    bitmap = FileUtils.getBitmap(getQrCodeFilepath(), options);
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
                    Timber.i("Saving QR Code to disk... : " + getQrCodeFilepath());
                    FileUtils.saveBitmapToFile(bitmap, getQrCodeFilepath());

                    FileUtils.write(mdCacheFile, messageDigest);
                    Timber.i("Updated %s file contents", SETTINGS_MD5_FILE);
                }

                // Send the task completion event
                emitter.onComplete();
            }
        });
    }

    @Override
    public String getQrCodeFilepath() {
        return new StoragePathProvider().getDirPath(StorageSubdirectory.SETTINGS) + File.separator + "collect-settings.png";
    }

    @Override
    public String getMd5CachePath() {
        return new StoragePathProvider().getDirPath(StorageSubdirectory.SETTINGS) + File.separator + SETTINGS_MD5_FILE;
    }
}
