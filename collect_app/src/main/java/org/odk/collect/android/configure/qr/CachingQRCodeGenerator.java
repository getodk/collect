package org.odk.collect.android.configure.qr;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import timber.log.Timber;

public class CachingQRCodeGenerator implements QRCodeGenerator {

    private static final String SETTINGS_MD5_FILE = ".collect-settings-hash";

    @Override
    public String generateQRCode(Collection<String> includedPasswordKeys, AppConfigurationGenerator appConfigurationGenerator) throws NoSuchAlgorithmException, IOException, WriterException {
        String preferencesString = appConfigurationGenerator.getAppConfigurationAsJson(includedPasswordKeys);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(preferencesString.getBytes());
        byte[] messageDigest = md.digest();

        boolean shouldWriteToDisk = true;

        // check if settings directory exists, if not then create one
        File writeDir = new File(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.SETTINGS));
        if (!writeDir.exists()) {
            if (!writeDir.mkdirs()) {
                Timber.e("Error creating directory %s", writeDir.getAbsolutePath());
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
                shouldWriteToDisk = false;
            }
        }

        // If the file is not found in the disk or md5Hash not matched
        if (shouldWriteToDisk) {
            Timber.i("Generating QRCode...");
            final long time = System.currentTimeMillis();
            Bitmap bmp = new QRCodeUtils().encode(preferencesString);
            Timber.i("QR Code generation took : %d ms", System.currentTimeMillis() - time);

            Timber.i("Saving QR Code to disk... : %s", getQRCodeFilepath());
            FileUtils.saveBitmapToFile(bmp, getQRCodeFilepath());

            FileUtils.write(mdCacheFile, messageDigest);
            Timber.i("Updated %s file contents", SETTINGS_MD5_FILE);
        }

        return getQRCodeFilepath();
    }

    private String getQRCodeFilepath() {
        return new StoragePathProvider().getOdkDirPath(StorageSubdirectory.SETTINGS) + File.separator + "collect-settings.png";
    }

    private String getMd5CachePath() {
        return new StoragePathProvider().getOdkDirPath(StorageSubdirectory.SETTINGS) + File.separator + SETTINGS_MD5_FILE;
    }
}
