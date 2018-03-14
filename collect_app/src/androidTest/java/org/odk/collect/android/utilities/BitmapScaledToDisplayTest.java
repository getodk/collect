package org.odk.collect.android.utilities;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;

import java.io.File;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class BitmapScaledToDisplayTest {

    @Test
    public void scaleDownBitmapWhenPossible() {
        new DownScaleImageTest()
                .createBitmap(1000, 1000)
                .scaleBitmapToDisplay(500, 500)
                .assertScaledBitmapDimensions(500, 500);

        new DownScaleImageTest()
                .createBitmap(600, 800)
                .scaleBitmapToDisplay(600, 200)
                .assertScaledBitmapDimensions(150, 200);

        new DownScaleImageTest()
                .createBitmap(500, 400)
                .scaleBitmapToDisplay(250, 200)
                .assertScaledBitmapDimensions(250, 200);

        new DownScaleImageTest()
                .createBitmap(2000, 800)
                .scaleBitmapToDisplay(300, 400)
                .assertScaledBitmapDimensions(333, 133);
    }

    @Test
    public void doNotScaleDownBitmapWhenNotPossible() {
        new DownScaleImageTest()
                .createBitmap(1000, 1000)
                .scaleBitmapToDisplay(2000, 2000)
                .assertScaledBitmapDimensions(1000, 1000);

        new DownScaleImageTest()
                .createBitmap(600, 800)
                .scaleBitmapToDisplay(600, 800)
                .assertScaledBitmapDimensions(600, 800);

        new DownScaleImageTest()
                .createBitmap(500, 400)
                .scaleBitmapToDisplay(600, 600)
                .assertScaledBitmapDimensions(500, 400);

        new DownScaleImageTest()
                .createBitmap(2000, 800)
                .scaleBitmapToDisplay(4000, 2000)
                .assertScaledBitmapDimensions(2000, 800);
    }

    class DownScaleImageTest {
        private final File cache = Collect.getInstance().getApplicationContext().getExternalCacheDir();
        private final File imageFile = new File(cache, "testImage.jpeg");
        private Bitmap scaledBitmap;

        DownScaleImageTest createBitmap(int imageHeight, int imageWidth) {
            Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
            FileUtils.saveBitmapToFile(bitmap, imageFile.getAbsolutePath());
            return this;
        }

        DownScaleImageTest scaleBitmapToDisplay(int windowHeight, int windowWidth) {
            scaledBitmap = FileUtils.getBitmapScaledToDisplay(imageFile, windowHeight, windowWidth);
            return this;
        }

        void assertScaledBitmapDimensions(int expectedHeight, int expectedWidth) {
            assertEquals(expectedHeight, scaledBitmap.getHeight());
            assertEquals(expectedWidth, scaledBitmap.getWidth());
        }
    }
}
