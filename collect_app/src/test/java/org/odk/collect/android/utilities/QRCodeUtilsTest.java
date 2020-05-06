package org.odk.collect.android.utilities;

import android.graphics.Bitmap;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.qr.ObservableQRCodeGenerator;
import org.odk.collect.android.preferences.qr.QRCodeGenerator;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DataFormatException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class QRCodeUtilsTest {

    private final QRCodeGenerator qrCodeGenerator = new ObservableQRCodeGenerator();
    private final File savedQrCodeImage = new File(qrCodeGenerator.getQrCodeFilepath());
    private final File md5File = new File(qrCodeGenerator.getMd5CachePath());

    @Before
    public void setup() {
        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
        AdminSharedPreferences.getInstance().loadDefaultPreferences();
        savedQrCodeImage.delete();
        md5File.delete();
    }

    @Test
    public void generateAndDecodeQRCode() throws IOException, WriterException, DataFormatException, ChecksumException, NotFoundException, FormatException {
        String data = "Some random text";
        Bitmap generatedQRBitMap = qrCodeGenerator.generateQRBitMap(data, 100);
        assertQRContains(generatedQRBitMap, data);
    }

    @Test
    public void generateQRCodeIfNoCacheExists() throws DataFormatException, FormatException, ChecksumException, NotFoundException, IOException, NoSuchAlgorithmException {
        // verify that QRCode and md5 cache files don't exist
        assertFalse(savedQrCodeImage.exists());
        assertFalse(md5File.exists());

        final GenerationResults generationResults = new GenerationResults();
        generateQrCode(generationResults);

        // assert files are saved
        assertTrue(savedQrCodeImage.exists());
        assertTrue(md5File.exists());

        String expectedData = "{\"general\":{},\"admin\":{}}";
        assertQRContains(generationResults.generatedBitmap.get(), expectedData);

        verifyCachedMd5Data(expectedData);
    }

    @Test
    public void readQRCodeFromDiskIfCacheExists() throws NoSuchAlgorithmException, IOException, WriterException {
        String expectedData = "{\"general\":{},\"admin\":{}}";

        // stubbing cache and bitmap files
        new File(new StoragePathProvider().getDirPath(StorageSubdirectory.SETTINGS)).mkdirs();
        FileUtils.saveBitmapToFile(qrCodeGenerator.generateQRBitMap(expectedData, 100), qrCodeGenerator.getQrCodeFilepath());
        FileUtils.write(md5File, getDigest(expectedData.getBytes()));

        // verify that QRCode and md5 cache files exist
        assertTrue(savedQrCodeImage.exists());
        assertTrue(md5File.exists());

        final long lastModifiedQRCode = savedQrCodeImage.lastModified();
        final long lastModifiedCache = md5File.lastModified();

        final GenerationResults generationResults = new GenerationResults();
        generateQrCode(generationResults);

        // assert that files were not modified
        assertEquals(lastModifiedCache, md5File.lastModified());
        assertEquals(lastModifiedQRCode, savedQrCodeImage.lastModified());

        verifyCachedMd5Data(expectedData);
    }

    public void generateQrCode(GenerationResults generationResults) {
        // subscribe to the QRCode generator in the same thread
        qrCodeGenerator.generateQRCode(new ArrayList<>())
                .subscribe(generationResults.generatedBitmap::set, generationResults.errorThrown::set, () -> generationResults.isFinished.set(true));

        generationResults.assertGeneratedOk();
    }

    /**
     * Verifies that the md5 data in the cached file is correct
     */
    private void verifyCachedMd5Data(String expectedData) throws NoSuchAlgorithmException {
        assertCachedFileIsCorrect(expectedData.getBytes(), md5File);
    }

    @SuppressWarnings("PMD.UseAssertEqualsInsteadOfAssertTrue")
    private void assertCachedFileIsCorrect(byte[] data, File file) throws NoSuchAlgorithmException {
        byte[] messageDigest = getDigest(data);
        byte[] cachedMessageDigest = FileUtils.read(file);
        assertTrue(Arrays.equals(messageDigest, cachedMessageDigest));
    }

    private byte[] getDigest(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data);
        return md.digest();
    }

    private void assertQRContains(Bitmap bitmap, String data) throws DataFormatException, FormatException, ChecksumException, NotFoundException, IOException {
        assertNotNull(bitmap);
        String result = QRCodeUtils.decodeFromBitmap(bitmap);
        assertEquals(data, result);
    }

    static class GenerationResults {
        final AtomicBoolean isFinished = new AtomicBoolean(false);
        final AtomicReference<Bitmap> generatedBitmap = new AtomicReference<>();
        final AtomicReference<Throwable> errorThrown = new AtomicReference<>();

        private void assertGeneratedOk() {
            assertNotNull(generatedBitmap.get());
            assertNull(errorThrown.get());
            assertTrue(isFinished.get());
        }
    }
}
