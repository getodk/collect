package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.widgets.QuestionWidget
import timber.log.Timber

class ImageCompressionController(private val imageCompressor: ImageCompressor) {
    fun execute(
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
            imageCompressor.execute(imagePath, maxPixels)
        }
    }

    private fun getMaxPixelsFromFormIfDefined(questionWidget: QuestionWidget): Int? {
        for (bindAttribute in questionWidget.formEntryPrompt.bindAttributes) {
            if ("max-pixels" == bindAttribute.name && ApplicationConstants.Namespaces.XML_OPENROSA_NAMESPACE == bindAttribute.namespace) {
                try {
                    return bindAttribute.attributeValue.toInt()
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
}
