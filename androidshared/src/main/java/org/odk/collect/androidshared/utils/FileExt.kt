package org.odk.collect.androidshared.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import timber.log.Timber
import java.io.File
import kotlin.use

fun File.getVideoThumbnail(context: Context): Bitmap? {
    return try {
        MediaMetadataRetriever().apply {
            setDataSource(context, this@getVideoThumbnail.toUri())
        }.use { retriever ->
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        }
    } catch (e: Exception) {
        Timber.w(e)
        null
    }
}
