/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.support.test.runner.AndroidJUnit4;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.DaggerAndroidTest;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.widgets.ImageWidget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.XML_OPENROSA_NAMESPACE;

@RunWith(AndroidJUnit4.class)
public class ImageConverterTest extends DaggerAndroidTest {
    private static final String TEST_DIR = Collect.INSTANCES_PATH + File.separator + "testForm_2017-10-12_19-36-15" + File.separator;
    private static final String TEST_IMAGE_PATH = TEST_DIR + "testImage.jpg";

    @Inject
    GeneralSharedPreferences generalSharedPreferences;

    @Inject
    ImageConverter imageConverter;

    @Override
    protected void injectDependencies() {
        androidTestComponent.inject(this);
    }

    @Before
    public void setUp() throws IOException {
        super.setUp();
        File wallpaperDirectory = new File(Collect.INSTANCES_PATH + File.separator + "testForm_2017-10-12_19-36-15" + File.separator);
        wallpaperDirectory.mkdirs();
    }

    @Test
    public void executeConversionWithoutAnySettings() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly1() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(4000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(2000, image.getWidth());
        assertEquals(1500, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly2() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(1500, image.getWidth());
        assertEquals(2000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly3() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(2000, image.getWidth());
        assertEquals(2000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly4() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "3000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly5() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "4000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly6() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2998"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(2998, image.getWidth());
        assertEquals(2998, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly7() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", ""), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly8() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget("", "max-pixels", "2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly9() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixel", "2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly10() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000.5"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly11() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "0"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly12() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "-2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly1() {
        generalSharedPreferences.save("image_size", "very_small");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(640, image.getWidth());
        assertEquals(640, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly2() {
        generalSharedPreferences.save("image_size", "small");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(1024, image.getWidth());
        assertEquals(1024, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly3() {
        generalSharedPreferences.save("image_size", "medium");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(2048, image.getWidth());
        assertEquals(2048, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly4() {
        generalSharedPreferences.save("image_size", "large");
        saveTestBitmap(3000, 3000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly5() {
        generalSharedPreferences.save("image_size", "large");
        saveTestBitmap(4000, 4000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3072, image.getWidth());
        assertEquals(3072, image.getHeight());
    }

    @Test
    public void scaleImageDownFormAndSettingsLevel1() {
        generalSharedPreferences.save("image_size", "small");
        saveTestBitmap(4000, 4000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(2000, image.getWidth());
        assertEquals(2000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormAndSettingsLevel2() {
        generalSharedPreferences.save("image_size", "small");
        saveTestBitmap(4000, 4000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "650"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(650, image.getWidth());
        assertEquals(650, image.getHeight());
    }

    @Test
    public void rotateImage1() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000, ExifInterface.ORIENTATION_ROTATE_90);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(4000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void rotateImage2() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000, ExifInterface.ORIENTATION_ROTATE_270);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(4000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void rotateImage3() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000, ExifInterface.ORIENTATION_ROTATE_180);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(4000, image.getHeight());
    }

    @Test
    public void rotateImage4() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000, ExifInterface.ORIENTATION_UNDEFINED);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(4000, image.getHeight());
    }

    @Test
    public void rotateImage5() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000, null);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(4000, image.getHeight());
    }

    @Test
    public void rotateAndScaleDownImage() {
        generalSharedPreferences.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000, ExifInterface.ORIENTATION_ROTATE_90);
        imageConverter.execute(TEST_IMAGE_PATH, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance());

        Bitmap image = FileUtils.getBitmap(TEST_IMAGE_PATH, new BitmapFactory.Options());
        assertEquals(2000, image.getWidth());
        assertEquals(1500, image.getHeight());
    }

    private void saveTestBitmap(int width, int height, Integer orientation) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        FileUtils.saveBitmapToFile(bitmap, TEST_IMAGE_PATH);

        if (orientation != null) {
            try {
                ExifInterface exifInterface = new ExifInterface(TEST_IMAGE_PATH);
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(orientation));
                exifInterface.saveAttributes();
            } catch (IOException e) {
                Timber.w(e);
            }
        }
    }

    private ImageWidget getTestImageWidget() {
        return getTestImageWidget(new ArrayList<TreeElement>());
    }

    private ImageWidget getTestImageWidget(String namespace, String name, String value) {
        List<TreeElement> bindAttributes = new ArrayList<>();
        bindAttributes.add(TreeElement.constructAttributeElement(namespace, name, value));

        return getTestImageWidget(bindAttributes);
    }

    private ImageWidget getTestImageWidget(List<TreeElement> bindAttributes) {
        FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

        when(formEntryPrompt.getBindAttributes()).thenReturn(bindAttributes);

        ImageWidget imageWidget = mock(ImageWidget.class);
        when(imageWidget.getFormEntryPrompt()).thenReturn(formEntryPrompt);

        return imageWidget;
    }
}
