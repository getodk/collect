package org.odk.collect.glide

import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.odk.collect.glide.svg.SvgSoftwareLayerSetter
import java.io.File

object ImageLoader {

    @JvmStatic
    fun ImageView.loadImage(imageFile: File) {
        if (imageFile.name.endsWith("svg")) {
            Glide.with(this)
                .`as`(PictureDrawable::class.java)
                .listener(SvgSoftwareLayerSetter())
                .load(imageFile)
                .centerInside()
                .into(this)
        } else {
            Glide.with(this)
                .load(imageFile)
                .centerInside()
                .into(this)
        }
    }
}
