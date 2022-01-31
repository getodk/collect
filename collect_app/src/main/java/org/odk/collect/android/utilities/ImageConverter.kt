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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import org.odk.collect.android.R
import org.odk.collect.android.widgets.QuestionWidget
import timber.log.Timber
import java.io.IOException

object ImageConverter {
    /**
     * Before proceed with scaling or rotating, make sure existing exif information is stored/restored.
     * @author Khuong Ninh (khuong.ninh@it-development.com)
     */
    @JvmStatic
    fun execute(
        imagePath: String,
        questionWidget: QuestionWidget,
        context: Context,
        imageSizeMode: String
    ) {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(imagePath)
        } catch (e: IOException) {
            Timber.w(e)
        }
        scaleDownImageIfNeeded(imagePath, questionWidget, context, imageSizeMode)
        if (exif != null) {
            try {
                exif.saveAttributes()
            } catch (e: IOException) {
                Timber.w(e)
            }
        }
    }

    private fun scaleDownImageIfNeeded(
        imagePath: String,
        questionWidget: QuestionWidget,
        context: Context,
        imageSizeMode: String
    ) {
        var maxPixels: Int?
        maxPixels = getMaxPixelsFromFormIfDefined(questionWidget)
        if (maxPixels == null) {
            maxPixels = getMaxPixelsFromSettings(context, imageSizeMode)
        }
        if (maxPixels != null && maxPixels > 0) {
            scaleDownImage(imagePath, maxPixels)
        }
    }

    private fun getMaxPixelsFromFormIfDefined(questionWidget: QuestionWidget): Int? {
        for (attrs in questionWidget.formEntryPrompt.bindAttributes) {
            if ("max-pixels" == attrs.name && ApplicationConstants.Namespaces.XML_OPENROSA_NAMESPACE == attrs.namespace) {
                try {
                    return attrs.attributeValue.toInt()
                } catch (e: NumberFormatException) {
                    Timber.i(e)
                }
            }
        }
        return null
    }

    private fun getMaxPixelsFromSettings(context: Context, imageSizeMode: String): Int? {
        val imageEntryValues = context.resources.getStringArray(R.array.image_size_entry_values)
        return when (imageSizeMode) {
            imageEntryValues[1] -> 640
            imageEntryValues[2] -> 1024
            imageEntryValues[3] -> 2048
            imageEntryValues[4] -> 3072
            else -> null
        }
    }

    /**
     * This method is used to reduce an original picture size.
     * maxPixels refers to the max pixels of the long edge, the short edge is scaled proportionately.
     */
    private fun scaleDownImage(imagePath: String, maxPixels: Int) {
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
}
