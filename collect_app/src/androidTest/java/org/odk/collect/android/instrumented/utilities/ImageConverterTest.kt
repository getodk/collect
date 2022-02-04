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
import android.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.odk.collect.android.TestSettingsProvider.getUnprotectedSettings
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.rules.RunnableRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.android.utilities.ApplicationConstants.Namespaces
import org.odk.collect.android.utilities.ImageConverter
import org.odk.collect.android.utilities.ImageFileUtils
import org.odk.collect.android.widgets.ImageWidget
import org.odk.collect.projects.Project
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.HashMap

@RunWith(AndroidJUnit4::class)
class ImageConverterTest {
    private lateinit var testImagePath: String
    private val generalSettings = getUnprotectedSettings()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(
            RunnableRule {
                // Set up demo project
                val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>())
                component.projectsRepository().save(Project.DEMO_PROJECT)
                component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID)
            }
        )

    @Before
    fun setUp() {
        testImagePath =
            StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES) + File.separator + "testForm_2017-10-12_19-36-15" + File.separator + "testImage.jpg"
        File(testImagePath).parentFile.mkdirs()
    }

    @Test
    fun executeConversionWithoutAnySettings() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_ORIGINAL)
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly1() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(4000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(2000, image!!.width)
        assertEquals(1500, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly2() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 4000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(1500, image!!.width)
        assertEquals(2000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly3() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(2000, image!!.width)
        assertEquals(2000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly4() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "3000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly5() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "4000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly6() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2998"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(2998, image!!.width)
        assertEquals(2998, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly7() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", ""),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly8() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget("", "max-pixels", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly9() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixel", "2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly10() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000.5"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly11() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "0"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownFormLevelOnly12() {
        generalSettings.save("image_size", "original_image_size")
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "-2000"),
            context,
            IMAGE_SIZE_ORIGINAL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownSettingsLevelOnly1() {
        generalSettings.save("image_size", IMAGE_SIZE_VERY_SMALL)
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_VERY_SMALL)
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(640, image!!.width)
        assertEquals(640, image.height)
    }

    @Test
    fun scaleImageDownSettingsLevelOnly2() {
        generalSettings.save("image_size", IMAGE_SIZE_SMALL)
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_SMALL)
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(1024, image!!.width)
        assertEquals(1024, image.height)
    }

    @Test
    fun scaleImageDownSettingsLevelOnly3() {
        generalSettings.save("image_size", IMAGE_SIZE_MEDIUM)
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_MEDIUM)
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(2048, image!!.width)
        assertEquals(2048, image.height)
    }

    @Test
    fun scaleImageDownSettingsLevelOnly4() {
        generalSettings.save("image_size", IMAGE_SIZE_LARGE)
        saveTestBitmap(3000, 3000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_LARGE)
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(3000, image.height)
    }

    @Test
    fun scaleImageDownSettingsLevelOnly5() {
        generalSettings.save("image_size", IMAGE_SIZE_LARGE)
        saveTestBitmap(4000, 4000)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_LARGE)
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3072, image!!.width)
        assertEquals(3072, image.height)
    }

    @Test
    fun scaleImageDownFormAndSettingsLevel1() {
        generalSettings.save("image_size", IMAGE_SIZE_SMALL)
        saveTestBitmap(4000, 4000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "2000"),
            context,
            IMAGE_SIZE_SMALL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(2000, image!!.width)
        assertEquals(2000, image.height)
    }

    @Test
    fun scaleImageDownFormAndSettingsLevel2() {
        generalSettings.save("image_size", "small")
        saveTestBitmap(4000, 4000)
        ImageConverter.execute(
            testImagePath,
            getTestImageWidget(Namespaces.XML_OPENROSA_NAMESPACE, "max-pixels", "650"),
            context,
            IMAGE_SIZE_SMALL
        )
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(650, image!!.width)
        assertEquals(650, image.height)
    }

    @Test
    fun keepExifAfterScaling() {
        val attributes: MutableMap<String, String> = HashMap()
        attributes[ExifInterface.TAG_ARTIST] = ExifInterface.TAG_ARTIST
        attributes[ExifInterface.TAG_DATETIME] = ExifInterface.TAG_DATETIME
        attributes[ExifInterface.TAG_DATETIME_ORIGINAL] = ExifInterface.TAG_DATETIME_ORIGINAL
        attributes[ExifInterface.TAG_DATETIME_DIGITIZED] = ExifInterface.TAG_DATETIME_DIGITIZED
        attributes[ExifInterface.TAG_GPS_ALTITUDE] = dec2DMS(-17.0)
        attributes[ExifInterface.TAG_GPS_ALTITUDE_REF] = ExifInterface.TAG_GPS_ALTITUDE_REF
        attributes[ExifInterface.TAG_GPS_DATESTAMP] = ExifInterface.TAG_GPS_DATESTAMP
        attributes[ExifInterface.TAG_GPS_LATITUDE] = dec2DMS(25.165173)
        attributes[ExifInterface.TAG_GPS_LATITUDE_REF] = ExifInterface.TAG_GPS_LATITUDE_REF
        attributes[ExifInterface.TAG_GPS_LONGITUDE] = dec2DMS(23.988174)
        attributes[ExifInterface.TAG_GPS_LONGITUDE_REF] = ExifInterface.TAG_GPS_LONGITUDE_REF
        attributes[ExifInterface.TAG_GPS_PROCESSING_METHOD] = ExifInterface.TAG_GPS_PROCESSING_METHOD
        attributes[ExifInterface.TAG_MAKE] = ExifInterface.TAG_MAKE
        attributes[ExifInterface.TAG_MODEL] = ExifInterface.TAG_MODEL
        attributes[ExifInterface.TAG_SUBSEC_TIME] = ExifInterface.TAG_SUBSEC_TIME

        saveTestBitmap(3000, 4000, attributes)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_ORIGINAL)
        val exifData = testImageExif
        assertNotNull(exifData)

        for (attributeName in attributes.keys) {
            when (attributeName) {
                ExifInterface.TAG_GPS_LATITUDE -> assertThat(
                    exifData!!.getAttribute(attributeName), `is`("25/1,9/1,54622/1000")
                )
                ExifInterface.TAG_GPS_LONGITUDE -> assertThat(
                    exifData!!.getAttribute(attributeName), `is`("23/1,59/1,17426/1000")
                )
                ExifInterface.TAG_GPS_ALTITUDE -> assertThat(
                    exifData!!.getAttribute(attributeName), `is`("17/1,0/1,0/1000")
                )
                else -> assertThat(exifData!!.getAttribute(attributeName), `is`(attributeName))
            }
        }
    }

    @Test
    fun verifyNoRotationAppliedForExifRotation() {
        val attributes: MutableMap<String, String> = HashMap()
        attributes[ExifInterface.TAG_ORIENTATION] = ExifInterface.ORIENTATION_ROTATE_90.toString()
        saveTestBitmap(3000, 4000, attributes)
        ImageConverter.execute(testImagePath, getTestImageWidget(), context, IMAGE_SIZE_ORIGINAL)
        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())
        assertEquals(3000, image!!.width)
        assertEquals(4000, image.height)
    }

    // https://stackoverflow.com/a/55252228/5479029
    private fun dec2DMS(coord: Double): String {
        var coord = coord
        coord = if (coord > 0) coord else -coord
        var out = "${coord.toInt()}/1,"
        coord = coord % 1 * 60
        out = "${out + coord.toInt()}/1,"
        coord = coord % 1 * 60000
        out = "${out + coord.toInt()}/1000"
        return out
    }

    private fun saveTestBitmap(
        width: Int,
        height: Int,
        attributes: Map<String, String> = HashMap()
    ) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        ImageFileUtils.saveBitmapToFile(bitmap, testImagePath)
        try {
            val exifInterface = ExifInterface(testImagePath)
            for (attributeName in attributes.keys) {
                exifInterface.setAttribute(attributeName, attributes[attributeName])
            }
            exifInterface.saveAttributes()
        } catch (e: IOException) {
            Timber.w(e)
        }
    }

    private val testImageExif: ExifInterface?
        get() {
            try {
                return ExifInterface(testImagePath)
            } catch (e: Exception) {
                Timber.w(e)
            }
            return null
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
