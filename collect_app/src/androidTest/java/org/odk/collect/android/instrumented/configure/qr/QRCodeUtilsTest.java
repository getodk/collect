package org.odk.collect.android.instrumented.configure.qr;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.configure.qr.QRCodeUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * It's important to test this in `androidTest` rather than `test` so that we make sure we're
 * testing real Bitmap decoding. If we use Robolectric we run into problems with `ShadowBitmapFactory`.
 */

@RunWith(AndroidJUnit4.class)
public class QRCodeUtilsTest {

    @Test
    public void canDecodeFromStream() throws Exception {
        String data = "Some random text";
        Bitmap bitmap = new QRCodeUtils().encode(data);
        ByteArrayInputStream inputStream = toStream(bitmap);

        String result = new QRCodeUtils().decode(inputStream);
        assertThat(result, is(data));
    }

    private ByteArrayInputStream toStream(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapData = bos.toByteArray();
        return new ByteArrayInputStream(bitmapData);
    }
}