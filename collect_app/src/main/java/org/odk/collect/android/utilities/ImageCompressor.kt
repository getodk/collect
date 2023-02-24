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
package org.odk.collect.android.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber

object ImageCompressor {
    /**
     * Before proceed with scaling or rotating, make sure existing exif information is stored/restored.
     * @author Khuong Ninh (khuong.ninh@it-development.com)
     */
    fun execute(imagePath: String, maxPixels: Int) {
        backupExifData(imagePath)
        scaleDownImage(imagePath, maxPixels)
        restoreExifData(imagePath)
    }

    /**
     * This method is used to reduce an original picture size.
     * maxPixels refers to the max pixels of the long edge, the short edge is scaled proportionately.
     */
    private fun scaleDownImage(imagePath: String, maxPixels: Int) {
        if (maxPixels <= 0) {
            return
        }

        var image = ImageFileUtils.getBitmap(imagePath, BitmapFactory.Options())
        if (image != null) {
            val originalWidth = image.width.toDouble()
            val originalHeight = image.height.toDouble()
            if (originalWidth > originalHeight && originalWidth > maxPixels) {
                val newHeight = (originalHeight / (originalWidth / maxPixels)).toInt()
                image = Bitmap.createScaledBitmap(image, maxPixels, newHeight, false)
                ImageFileUtils.saveBitmapToFile(image, imagePath)
            } else if (originalHeight > maxPixels) {
                val newWidth = (originalWidth / (originalHeight / maxPixels)).toInt()
                image = Bitmap.createScaledBitmap(image, newWidth, maxPixels, false)
                ImageFileUtils.saveBitmapToFile(image, imagePath)
            }
        }
    }

    private fun backupExifData(imagePath: String) {
        try {
            val exif = ExifInterface(imagePath)
            for ((key, _) in exifDataBackup) {
                exifDataBackup[key] = exif.getAttribute(key)
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun restoreExifData(imagePath: String) {
        try {
            val exif = ExifInterface(imagePath)
            for ((key, value) in exifDataBackup) {
                exif.setAttribute(key, value)
            }
            exif.saveAttributes()
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private val exifDataBackup = mutableMapOf<String, String?>(
        ExifInterface.TAG_DATETIME to null,
        ExifInterface.TAG_DATETIME_ORIGINAL to null,
        ExifInterface.TAG_DATETIME_DIGITIZED to null,
        ExifInterface.TAG_OFFSET_TIME to null,
        ExifInterface.TAG_OFFSET_TIME_ORIGINAL to null,
        ExifInterface.TAG_OFFSET_TIME_DIGITIZED to null,
        ExifInterface.TAG_SUBSEC_TIME to null,
        ExifInterface.TAG_SUBSEC_TIME_ORIGINAL to null,
        ExifInterface.TAG_SUBSEC_TIME_DIGITIZED to null,
        ExifInterface.TAG_IMAGE_DESCRIPTION to null,
        ExifInterface.TAG_MAKE to null,
        ExifInterface.TAG_MODEL to null,
        ExifInterface.TAG_SOFTWARE to null,
        ExifInterface.TAG_ARTIST to null,
        ExifInterface.TAG_COPYRIGHT to null,
        ExifInterface.TAG_MAKER_NOTE to null,
        ExifInterface.TAG_USER_COMMENT to null,
        ExifInterface.TAG_IMAGE_UNIQUE_ID to null,
        ExifInterface.TAG_CAMERA_OWNER_NAME to null,
        ExifInterface.TAG_BODY_SERIAL_NUMBER to null,
        ExifInterface.TAG_GPS_ALTITUDE to null,
        ExifInterface.TAG_GPS_ALTITUDE_REF to null,
        ExifInterface.TAG_GPS_DATESTAMP to null,
        ExifInterface.TAG_GPS_TIMESTAMP to null,
        ExifInterface.TAG_GPS_LATITUDE to null,
        ExifInterface.TAG_GPS_LATITUDE_REF to null,
        ExifInterface.TAG_GPS_LONGITUDE to null,
        ExifInterface.TAG_GPS_LONGITUDE_REF to null,
        ExifInterface.TAG_GPS_SATELLITES to null,
        ExifInterface.TAG_GPS_STATUS to null,
        ExifInterface.TAG_ORIENTATION to null
    )
}
