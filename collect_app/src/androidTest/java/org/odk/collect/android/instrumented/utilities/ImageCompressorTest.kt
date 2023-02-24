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
package org.odk.collect.android.instrumented.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.odk.collect.android.utilities.ApplicationConstants.Namespaces
import org.odk.collect.android.utilities.ImageConverter
import org.odk.collect.android.utilities.ImageFileUtils
import org.odk.collect.android.widgets.ImageWidget
import timber.log.Timber
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ImageConverterTest {
    private lateinit var testImagePath: String
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun executeConversionWithoutAnySettings() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_ORIGINAL)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly1() {
        saveTestBitmap(4000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2000, equalTo(image.width))
        assertThat(1500, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly2() {
        saveTestBitmap(3000, 4000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(1500, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly3() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2000, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly4() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "3000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly5() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "4000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly6() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2998"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2998, equalTo(image.width))
        assertThat(2998, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly7() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", ""),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly8() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget("", "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly9() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixel", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly10() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000.5"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly11() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "0"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormLevelOnly12() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "-2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownSettingsLevelOnly1() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_VERY_SMALL)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(640, equalTo(image.width))
        assertThat(640, equalTo(image.height))
    }

    @Test
    fun scaleImageDownSettingsLevelOnly2() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_SMALL)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(1024, equalTo(image.width))
        assertThat(1024, equalTo(image.height))
    }

    @Test
    fun scaleImageDownSettingsLevelOnly3() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_MEDIUM)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2048, equalTo(image.width))
        assertThat(2048, equalTo(image.height))
    }

    @Test
    fun scaleImageDownSettingsLevelOnly4() {
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_LARGE)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownSettingsLevelOnly5() {
        saveTestBitmap(4000, 4000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_LARGE)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3072, equalTo(image.width))
        assertThat(3072, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormAndSettingsLevel1() {
        saveTestBitmap(4000, 4000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_SMALL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2000, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun scaleImageDownFormAndSettingsLevel2() {
        saveTestBitmap(4000, 4000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "650"),
            context,
            IMAGE_SIZE_SMALL
        )

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(650, equalTo(image.width))
        assertThat(650, equalTo(image.height))
    }

    @Test
    fun keepExifAfterScaling() {
        val attributes = mutableMapOf(
            ExifInterface.TAG_DATETIME to "2014:01:23 14:57:18",
            ExifInterface.TAG_DATETIME_ORIGINAL to "2014:01:23 14:57:18",
            ExifInterface.TAG_DATETIME_DIGITIZED to "2014:01:23 14:57:18",
            ExifInterface.TAG_OFFSET_TIME to "+1:00",
            ExifInterface.TAG_OFFSET_TIME_ORIGINAL to "+1:00",
            ExifInterface.TAG_OFFSET_TIME_DIGITIZED to "+1:00",
            ExifInterface.TAG_SUBSEC_TIME to "First photo",
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL to "0",
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED to "0",
            ExifInterface.TAG_IMAGE_DESCRIPTION to "Photo from Poland",
            ExifInterface.TAG_MAKE to "OLYMPUS IMAGING CORP",
            ExifInterface.TAG_MODEL to "STYLUS1",
            ExifInterface.TAG_SOFTWARE to "Version 1.0",
            ExifInterface.TAG_ARTIST to "Grzegorz",
            ExifInterface.TAG_COPYRIGHT to "G",
            ExifInterface.TAG_MAKER_NOTE to "OLYMPUS",
            ExifInterface.TAG_USER_COMMENT to "First photo",
            ExifInterface.TAG_IMAGE_UNIQUE_ID to "123456789",
            ExifInterface.TAG_CAMERA_OWNER_NAME to "John",
            ExifInterface.TAG_BODY_SERIAL_NUMBER to "987654321",
            ExifInterface.TAG_GPS_ALTITUDE to "41/1",
            ExifInterface.TAG_GPS_ALTITUDE_REF to "0",
            ExifInterface.TAG_GPS_DATESTAMP to "2014:01:23",
            ExifInterface.TAG_GPS_TIMESTAMP to "14:57:18",
            ExifInterface.TAG_GPS_LATITUDE to "50/1,49/1,8592/1000",
            ExifInterface.TAG_GPS_LATITUDE_REF to "N",
            ExifInterface.TAG_GPS_LONGITUDE to "0/1,8/1,12450/1000",
            ExifInterface.TAG_GPS_LONGITUDE_REF to "W",
            ExifInterface.TAG_GPS_SATELLITES to "8",
            ExifInterface.TAG_GPS_STATUS to "A",
            ExifInterface.TAG_ORIENTATION to "1"
        )

        saveTestBitmap(3000, 4000, attributes)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_VERY_SMALL)

        val exifData = ExifInterface(testImagePath)
        for (attributeName in attributes.keys) {
            assertThat(exifData.getAttribute(attributeName), equalTo(attributes[attributeName]))
        }
    }

    @Test
    fun verifyNoRotationAppliedForExifRotation() {
        val attributes = mapOf(ExifInterface.TAG_ORIENTATION to ExifInterface.ORIENTATION_ROTATE_90.toString())
        saveTestBitmap(3000, 4000, attributes)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_ORIGINAL)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(4000, equalTo(image.height))
    }

    private fun saveTestBitmap(
        width: Int,
        height: Int,
        attributes: Map<String, String> = emptyMap()
    ) {
        testImagePath = File.createTempFile("test", ".jpg").absolutePath

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        ImageFileUtils.saveBitmapToFile(bitmap, testImagePath)
        try {
            val exifInterface = ExifInterface(testImagePath)
            for ((key, value) in attributes) {
                exifInterface.setAttribute(key, value)
            }
            exifInterface.saveAttributes()
        } catch (e: IOException) {
            Timber.w(e)
        }
    }

    private fun getTestImageWidget(namespace: String, name: String, value: String): ImageWidget {
        val bindAttributes: MutableList<TreeElement> = mutableListOf()
        bindAttributes.add(TreeElement.constructAttributeElement(namespace, name, value))
        return getTestImageWidget(bindAttributes)
    }

    private fun getTestImageWidget(bindAttributes: List<TreeElement> = emptyList()): ImageWidget {
        val formEntryPrompt = mock(FormEntryPrompt::class.java)
        `when`(formEntryPrompt.bindAttributes).thenReturn(bindAttributes)
        val imageWidget = mock(ImageWidget::class.java)
        `when`(imageWidget.formEntryPrompt).thenReturn(formEntryPrompt)
        return imageWidget
    }

    companion object {
        private const val IMAGE_SIZE_ORIGINAL = "original_image_size"
        private const val IMAGE_SIZE_LARGE = "large"
        private const val IMAGE_SIZE_MEDIUM = "medium"
        private const val IMAGE_SIZE_SMALL = "small"
        private const val IMAGE_SIZE_VERY_SMALL = "very_small"
    }
}
