package org.odk.collect.glide

import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.odk.collect.glide.svg.SvgSoftwareLayerSetter
import java.io.File

object ImageLoader {

    @JvmStatic
    fun ImageView.loadImage(imageFile: File?, scaleType: ImageView.ScaleType) {
        if (imageFile == null) {
            return
        }

        val requestOptions = RequestOptions().apply {
            setScaleType(scaleType)
        }

        if (imageFile.name != null && imageFile.name.endsWith("svg")) {
            Glide.with(this)
                .`as`(PictureDrawable::class.java)
                .listener(SvgSoftwareLayerSetter())
                .load(imageFile)
                .apply(requestOptions)
                .into(this)
        } else {
            Glide.with(this)
                .load(imageFile)
                .apply(requestOptions)
                .into(this)
        }
    }
}
