package org.odk.collect.androidshared.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import timber.log.Timber
import java.io.File

/**
 * Calculates the [BitmapFactory.Options.inSampleSize] to use when decoding this image file so
 * that neither dimension of the decoded bitmap exceeds [maxDimension]. Only the image bounds
 * are read here, so no bitmap is allocated. Because [BitmapFactory.Options.inSampleSize] is a
 * power of 2, the decoded bitmap can end up smaller than [maxDimension] (an image just over
 * the limit will be halved), but it is never larger.
 */
fun File.calculateInSampleSize(maxDimension: Int): Int {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(absolutePath, options)

    var inSampleSize = 1
    while (options.outWidth / inSampleSize > maxDimension || options.outHeight / inSampleSize > maxDimension) {
        inSampleSize *= 2
    }
    return inSampleSize
}

fun File.getVideoThumbnail(context: Context): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, this.toUri())
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (e: Exception) {
        Timber.w(e)
        null
    } finally {
        retriever.release()
    }
}
