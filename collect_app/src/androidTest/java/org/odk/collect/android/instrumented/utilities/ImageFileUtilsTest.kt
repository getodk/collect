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
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.utilities.ImageFileUtils
import timber.log.Timber
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ImageFileUtilsTest {

    private lateinit var sourceFile: File
    private lateinit var destinationFile: File
    private lateinit var attributes: MutableMap<String, String>

    @Before
    fun setup() {
        sourceFile = createTempImageFile("source")
        destinationFile = createTempImageFile("destination")
        attributes = HashMap()
    }

    @Test
    fun copyAndRotateImageNinety() {
        attributes[ExifInterface.TAG_ORIENTATION] = ExifInterface.ORIENTATION_ROTATE_90.toString()
        saveTestBitmapToFile(sourceFile.absolutePath, attributes)
        ImageFileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile)
        val image = ImageFileUtils.getBitmap(destinationFile.absolutePath, BitmapFactory.Options())!!

        assertEquals(2, image.width)
        assertEquals(1, image.height)
        assertEquals(Color.GREEN, image.getPixel(0, 0))
        assertEquals(Color.RED, image.getPixel(1, 0))
        verifyNoExifOrientationInDestinationFile(destinationFile.absolutePath)
    }

    @Test
    fun copyAndRotateImageTwoSeventy() {
        attributes[ExifInterface.TAG_ORIENTATION] = ExifInterface.ORIENTATION_ROTATE_270.toString()
        saveTestBitmapToFile(sourceFile.absolutePath, attributes)
        ImageFileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile)
        val image = ImageFileUtils.getBitmap(destinationFile.absolutePath, BitmapFactory.Options())!!

        assertEquals(2, image.width)
        assertEquals(1, image.height)
        assertEquals(Color.RED, image.getPixel(0, 0))
        assertEquals(Color.GREEN, image.getPixel(1, 0))
        verifyNoExifOrientationInDestinationFile(destinationFile.absolutePath)
    }

    @Test
    fun copyAndRotateImageOneEighty() {
        attributes[ExifInterface.TAG_ORIENTATION] = ExifInterface.ORIENTATION_ROTATE_180.toString()
        saveTestBitmapToFile(sourceFile.absolutePath, attributes)
        ImageFileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile)
        val image = ImageFileUtils.getBitmap(destinationFile.absolutePath, BitmapFactory.Options())!!

        assertEquals(1, image.width)
        assertEquals(2, image.height)
        assertEquals(Color.GREEN, image.getPixel(0, 0))
        assertEquals(Color.RED, image.getPixel(0, 1))
        verifyNoExifOrientationInDestinationFile(destinationFile.absolutePath)
    }

    @Test
    fun copyAndRotateImageUndefined() {
        attributes[ExifInterface.TAG_ORIENTATION] = ExifInterface.ORIENTATION_UNDEFINED.toString()
        saveTestBitmapToFile(sourceFile.absolutePath, attributes)
        ImageFileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile)
        val image = ImageFileUtils.getBitmap(destinationFile.absolutePath, BitmapFactory.Options())!!

        assertEquals(1, image.width)
        assertEquals(2, image.height)
        assertEquals(Color.RED, image.getPixel(0, 0))
        assertEquals(Color.GREEN, image.getPixel(0, 1))
        verifyNoExifOrientationInDestinationFile(destinationFile.absolutePath)
    }

    @Test
    fun copyAndRotateImageNoExif() {
        saveTestBitmapToFile(sourceFile.absolutePath, null)
        ImageFileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile)
        val image = ImageFileUtils.getBitmap(destinationFile.absolutePath, BitmapFactory.Options())!!

        assertEquals(1, image.width)
        assertEquals(2, image.height)
        assertEquals(Color.RED, image.getPixel(0, 0))
        assertEquals(Color.GREEN, image.getPixel(0, 1))
        verifyNoExifOrientationInDestinationFile(destinationFile.absolutePath)
    }

    /**
     * These cases all have a window smaller than the image so the image should be scaled down.
     * Note that the scaling isn't exact -- the factor is the closest power of 2 to the exact one.
     */
    @Test
    fun scaleDownBitmapWhenNeeded() {
        runScaleTest(1000, 1000, 500, 500, 500, 500, false)
        runScaleTest(600, 800, 600, 200, 150, 200, false)
        runScaleTest(500, 400, 250, 200, 250, 200, false)
    }

    @Test
    fun doNotScaleDownBitmapWhenNotNeeded() {
        runScaleTest(1000, 1000, 2000, 2000, 1000, 1000, false)
        runScaleTest(600, 800, 600, 800, 600, 800, false)
        runScaleTest(500, 400, 600, 600, 500, 400, false)
        runScaleTest(2000, 800, 4000, 2000, 2000, 800, false)
    }

    @Test
    fun accuratelyScaleBitmapToDisplay() {
        runScaleTest(1000, 1000, 500, 500, 500, 500, true)
        runScaleTest(600, 800, 600, 200, 150, 200, true)
        runScaleTest(500, 400, 250, 200, 250, 200, true)
        runScaleTest(2000, 800, 300, 400, 300, 120, true)
        runScaleTest(1000, 1000, 2000, 2000, 2000, 2000, true)
        runScaleTest(600, 800, 600, 800, 600, 800, true)
        runScaleTest(500, 400, 600, 600, 600, 480, true)
        runScaleTest(2000, 800, 4000, 2000, 4000, 1600, true)
    }

    private fun runScaleTest(
        imageHeight: Int,
        imageWidth: Int,
        windowHeight: Int,
        windowWidth: Int,
        expectedHeight: Int,
        expectedWidth: Int,
        shouldScaleAccurately: Boolean
    ) {
        ScaleImageTest()
            .createBitmap(imageHeight, imageWidth)
            .scaleBitmapToDisplay(windowHeight, windowWidth, shouldScaleAccurately)
            .assertScaledBitmapDimensions(expectedHeight, expectedWidth)
    }

    private fun verifyNoExifOrientationInDestinationFile(destinationFilePath: String) {
        val exifData = getTestImageExif(destinationFilePath)
        if (exifData != null) {
            assertEquals(
                ExifInterface.ORIENTATION_UNDEFINED,
                exifData.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
                )
            )
        }
    }

    private fun saveTestBitmapToFile(
        filePath: String,
        attributes: Map<String, String>?
    ) {
        val bitmap = Bitmap.createBitmap(1, 2, Bitmap.Config.ARGB_8888)
        bitmap.setPixel(0, 0, Color.RED)
        bitmap.setPixel(0, 1, Color.GREEN)

        ImageFileUtils.saveBitmapToFile(bitmap, filePath)
        if (attributes != null) {
            try {
                val exifInterface = ExifInterface(filePath)
                for (attributeName in attributes.keys) {
                    exifInterface.setAttribute(attributeName, attributes[attributeName])
                }
                exifInterface.saveAttributes()
            } catch (e: IOException) {
                Timber.w(e)
            }
        }
    }

    private fun getTestImageExif(imagePath: String): ExifInterface? {
        try {
            return ExifInterface(imagePath)
        } catch (e: Exception) {
            Timber.w(e)
        }
        return null
    }

    private fun createTempImageFile(imageName: String): File {
        val temp = File.createTempFile(imageName, ".png")
        temp.deleteOnExit()
        return temp
    }

    private class ScaleImageTest {
        private val cache = ApplicationProvider.getApplicationContext<Context>().externalCacheDir
        private val imageFile = File(cache, "testImage.jpeg")
        private var scaledBitmap: Bitmap? = null

        fun createBitmap(imageHeight: Int, imageWidth: Int): ScaleImageTest {
            val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            ImageFileUtils.saveBitmapToFile(bitmap, imageFile.absolutePath)
            return this
        }

        fun scaleBitmapToDisplay(
            windowHeight: Int,
            windowWidth: Int,
            shouldScaleAccurately: Boolean
        ): ScaleImageTest {
            scaledBitmap = ImageFileUtils.getBitmapScaledToDisplay(
                imageFile,
                windowHeight,
                windowWidth,
                shouldScaleAccurately
            )
            return this
        }

        fun assertScaledBitmapDimensions(expectedHeight: Int, expectedWidth: Int) {
            assertEquals(expectedHeight.toLong(), scaledBitmap!!.height.toLong())
            assertEquals(expectedWidth.toLong(), scaledBitmap!!.width.toLong())
        }
    }
}
