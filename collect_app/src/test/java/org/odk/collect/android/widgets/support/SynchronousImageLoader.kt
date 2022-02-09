package org.odk.collect.android.widgets.support

import android.graphics.BitmapFactory
import android.widget.ImageView
import org.odk.collect.glide.ImageLoader
import org.odk.collect.glide.ImageLoaderImpl
import java.io.File

class SynchronousImageLoader : ImageLoader {
    override fun loadImage(
        imageView: ImageView,
        imageFile: File?,
        scaleType: ImageView.ScaleType,
        requestListener: ImageLoaderImpl.ImageLoaderCallback?
    ) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile?.absolutePath))
    }
}
