package org.odk.collect.android.utilities;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class QRCodeUtilsTest {

    @Test
    public void encodeAndDecodeWorks() throws Exception {
        String data = "Some random text";
        Bitmap generatedQRBitMap = QRCodeUtils.encode(data);
        assertNotNull(generatedQRBitMap);

        String result = QRCodeUtils.decodeFromBitmap(generatedQRBitMap);
        assertEquals(data, result);
    }

}
