package org.odk.collect.android.widgets.support

import android.graphics.BitmapFactory
import android.widget.ImageView
import org.odk.collect.imageloader.GlideImageLoader
import org.odk.collect.imageloader.ImageLoader
import java.io.File

class SynchronousImageLoader : ImageLoader {
    override fun loadImage(
        imageView: ImageView,
        imageFile: File?,
        scaleType: ImageView.ScaleType,
        requestListener: GlideImageLoader.ImageLoaderCallback?
    ) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile?.absolutePath))
    }
}
