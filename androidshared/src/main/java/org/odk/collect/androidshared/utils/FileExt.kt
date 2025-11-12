package org.odk.collect.androidshared.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import timber.log.Timber
import java.io.File

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
