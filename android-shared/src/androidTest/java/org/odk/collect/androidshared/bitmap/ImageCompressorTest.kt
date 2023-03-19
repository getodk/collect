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
package org.odk.collect.androidshared.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ImageCompressorTest {
    private lateinit var testImagePath: String
    private val imageCompressor = ImageCompressor

    @Test
    fun imageShouldNotBeChangedIfMaxPixelsIsZero() {
        saveTestBitmap(3000, 2000)
        imageCompressor.execute(testImagePath, 0)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun imageShouldNotBeChangedIfMaxPixelsIsSmallerThanZero() {
        saveTestBitmap(3000, 2000)
        imageCompressor.execute(testImagePath, -10)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun imageShouldNotBeChangedIfMaxPixelsIsNotSmallerThanTheEdgeWhenWidthIsBiggerThanHeight() {
        saveTestBitmap(3000, 2000)
        imageCompressor.execute(testImagePath, 3000)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun imageShouldNotBeChangedIfMaxPixelsIsNotSmallerThanTheLongEdgeWhenWidthIsSmallerThanHeight() {
        saveTestBitmap(2000, 3000)
        imageCompressor.execute(testImagePath, 4000)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun imageShouldNotBeChangedIfMaxPixelsIsNotSmallerThanTheLongEdgeWhenWidthEqualsHeight() {
        saveTestBitmap(3000, 3000)
        imageCompressor.execute(testImagePath, 3000)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(3000, equalTo(image.height))
    }

    @Test
    fun imageShouldBeCompressedIfMaxPixelsIsSmallerThanTheLongEdgeWhenWidthIsBiggerThanHeight() {
        saveTestBitmap(4000, 3000)
        imageCompressor.execute(testImagePath, 2000)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2000, equalTo(image.width))
        assertThat(1500, equalTo(image.height))
    }

    @Test
    fun imageShouldBeCompressedIfMaxPixelsIsSmallerThanTheLongEdgeWhenWidthIsSmallerThanHeight() {
        saveTestBitmap(3000, 4000)
        imageCompressor.execute(testImagePath, 2000)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(1500, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun imageShouldBeCompressedIfMaxPixelsIsSmallerThanTheLongEdgeWhenWidthEqualsHeight() {
        saveTestBitmap(3000, 3000)
        imageCompressor.execute(testImagePath, 2000)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(2000, equalTo(image.width))
        assertThat(2000, equalTo(image.height))
    }

    @Test
    fun keepExifAfterScaling() {
        val attributes = mutableMapOf(
            // supported exif tags
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
            ExifInterface.TAG_ORIENTATION to "1",

            // unsupported exif tags
            ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH to "5",
            ExifInterface.TAG_DNG_VERSION to "100",
        )

        saveTestBitmap(3000, 4000, attributes)
        imageCompressor.execute(testImagePath, 2000)

        val exifData = ExifInterface(testImagePath)
        for (attributeName in attributes.keys) {
            if (attributeName == ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH ||
                attributeName == ExifInterface.TAG_DNG_VERSION
            ) {
                assertThat(exifData.getAttribute(attributeName), equalTo(null))
            } else {
                assertThat(exifData.getAttribute(attributeName), equalTo(attributes[attributeName]))
            }
        }
    }

    @Test
    fun verifyNoRotationAppliedForExifRotation() {
        val attributes = mapOf(ExifInterface.TAG_ORIENTATION to ExifInterface.ORIENTATION_ROTATE_90.toString())
        saveTestBitmap(3000, 4000, attributes)
        imageCompressor.execute(testImagePath, 4000)

        val image = ImageFileUtils.getBitmap(testImagePath, BitmapFactory.Options())!!

        assertThat(3000, equalTo(image.width))
        assertThat(4000, equalTo(image.height))
    }

    private fun saveTestBitmap(width: Int, height: Int, attributes: Map<String, String> = emptyMap()) {
        testImagePath = File.createTempFile("test", ".jpg").absolutePath

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        ImageFileUtils.saveBitmapToFile(bitmap, testImagePath)
        val exifInterface = ExifInterface(testImagePath)
        for ((key, value) in attributes) {
            exifInterface.setAttribute(key, value)
        }
        exifInterface.saveAttributes()
    }
}
