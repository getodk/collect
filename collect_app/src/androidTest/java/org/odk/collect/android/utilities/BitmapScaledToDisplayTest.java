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

    /**
     * These cases all have a window smaller than the image so the image should be scaled down.
     * Note that the scaling isn't exact -- the factor is the closest power of 2 to the exact one.
     */
    @Test
    @SuppressWarnings("ParenPad")
    public void scaleDownBitmapWhenNeeded() {
        runScaleTest(1000,   1000,    500,    500,    500,    500,    false);
        runScaleTest( 600,    800,    600,    200,    150,    200,    false);
        runScaleTest( 500,    400,    250,    200,    250,    200,    false);
        runScaleTest(2000,    800,    300,    400,    500,    200,    false);
    }

    @Test
    @SuppressWarnings("ParenPad")
    public void doNotScaleDownBitmapWhenNotNeeded() {
        runScaleTest(1000,   1000,    2000,   2000,   1000,   1000,   false);
        runScaleTest( 600,    800,     600,    800,    600,    800,   false);
        runScaleTest( 500,    400,     600,    600,    500,    400,   false);
        runScaleTest(2000,    800,    4000,   2000,   2000,    800,   false);
    }

    @Test
    @SuppressWarnings("ParenPad")
    public void accuratelyScaleBitmapToDisplay() {
        runScaleTest(1000,   1000,    500,    500,    500,    500,    true);
        runScaleTest( 600,    800,    600,    200,    150,    200,    true);
        runScaleTest( 500,    400,    250,    200,    250,    200,    true);
        runScaleTest(2000,    800,    300,    400,    300,    120,    true);
        runScaleTest(1000,   1000,   2000,   2000,   2000,   2000,    true);
        runScaleTest( 600,    800,    600,    800,    600,    800,    true);
        runScaleTest( 500,    400,    600,    600,    600,    480,    true);
        runScaleTest(2000,    800,   4000,   2000,   4000,   1600,    true);
    }

    private void runScaleTest(int imageHeight, int imageWidth, int windowHeight, int windowWidth, int expectedHeight, int expectedWidth, boolean shouldScaleAccurately) {
        new ScaleImageTest()
                .createBitmap(imageHeight, imageWidth)
                .scaleBitmapToDisplay(windowHeight, windowWidth, shouldScaleAccurately)
                .assertScaledBitmapDimensions(expectedHeight, expectedWidth);
    }

    private static class ScaleImageTest {
        private final File cache = Collect.getInstance().getApplicationContext().getExternalCacheDir();
        private final File imageFile = new File(cache, "testImage.jpeg");
        private Bitmap scaledBitmap;

        ScaleImageTest createBitmap(int imageHeight, int imageWidth) {
            Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
            FileUtils.saveBitmapToFile(bitmap, imageFile.getAbsolutePath());
            return this;
        }

        ScaleImageTest scaleBitmapToDisplay(int windowHeight, int windowWidth, boolean shouldScaleAccurately) {
            scaledBitmap = FileUtils.getBitmapScaledToDisplay(imageFile, windowHeight, windowWidth, shouldScaleAccurately);
            return this;
        }

        void assertScaledBitmapDimensions(int expectedHeight, int expectedWidth) {
            assertEquals(expectedHeight, scaledBitmap.getHeight());
            assertEquals(expectedWidth, scaledBitmap.getWidth());
        }
    }
}
