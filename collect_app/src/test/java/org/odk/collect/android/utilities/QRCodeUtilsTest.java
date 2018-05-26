package org.odk.collect.android.utilities;

import android.graphics.Bitmap;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.DaggerTest;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.TestComponent;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
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

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.utilities.QRCodeUtils.MD5_CACHE_PATH;
import static org.odk.collect.android.utilities.QRCodeUtils.QR_CODE_FILEPATH;


@RunWith(RobolectricTestRunner.class)
public class QRCodeUtilsTest extends DaggerTest {

    private static final String DEFAULT_SHARED_PREF_AFTER_APP_LOAD = "{\"general\":{},\"admin\":{}}";
    private final File savedQrCodeImage = new File(QR_CODE_FILEPATH);
    private final File md5File = new File(MD5_CACHE_PATH);

    @Inject
    GeneralSharedPreferences generalSharedPreferences;
    @Inject
    QRCodeUtils qrCodeUtils;

    @Override
    protected void injectDependencies(TestComponent testComponent) {
        testComponent.inject(this);
    }

    @Before
    public void setUp() {
        generalSharedPreferences.loadDefaultPreferences();
        savedQrCodeImage.delete();
        md5File.delete();
    }

    @Test
    public void generateAndDecodeQRCode() throws IOException, WriterException, DataFormatException, ChecksumException, NotFoundException, FormatException {
        String data = "Some random text";
        Bitmap generatedQRBitMap = QRCodeUtils.generateQRBitMap(data, 100);
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

        assertQRContains(generationResults.generatedBitmap.get(), DEFAULT_SHARED_PREF_AFTER_APP_LOAD);

        verifyCachedMd5Data(DEFAULT_SHARED_PREF_AFTER_APP_LOAD);
    }

    @Test
    public void readQRCodeFromDiskIfCacheExists() throws NoSuchAlgorithmException, IOException, WriterException {
        // stubbing cache and bitmap files
        new File(Collect.SETTINGS).mkdirs();
        FileUtils.saveBitmapToFile(QRCodeUtils.generateQRBitMap(DEFAULT_SHARED_PREF_AFTER_APP_LOAD, 100), QR_CODE_FILEPATH);
        FileUtils.write(md5File, getDigest(DEFAULT_SHARED_PREF_AFTER_APP_LOAD.getBytes()));

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

        verifyCachedMd5Data(DEFAULT_SHARED_PREF_AFTER_APP_LOAD);
    }

    public void generateQrCode(GenerationResults generationResults) {
        // subscribe to the QRCode generator in the same thread

        qrCodeUtils
                .getQRCodeGeneratorObservable(new ArrayList<>())
                .subscribe(
                        generationResults.generatedBitmap::set,
                        generationResults.errorThrown::set,
                        () -> generationResults.isFinished.set(true)
                );

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
