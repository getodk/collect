package org.odk.collect.glide

import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import org.odk.collect.glide.svg.SvgSoftwareLayerSetter
import java.io.File

object ImageLoader {

    @JvmStatic
    @JvmOverloads
    fun ImageView.loadImage(
        imageFile: File?,
        scaleType: ImageView.ScaleType,
        requestListener: ImageLoaderCallback? = null
    ) {
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
                .listener(object : RequestListener<PictureDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<PictureDrawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        requestListener?.onLoadFailed()
                        return false
                    }

                    override fun onResourceReady(
                        resource: PictureDrawable?,
                        model: Any?,
                        target: Target<PictureDrawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        requestListener?.onLoadSucceeded()
                        return false
                    }
                })
                .apply(requestOptions)
                .into(this)
        } else {
            Glide.with(this)
                .load(imageFile)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        requestListener?.onLoadFailed()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        requestListener?.onLoadSucceeded()
                        return false
                    }
                })
                .apply(requestOptions)
                .into(this)
        }
    }

    interface ImageLoaderCallback {
        fun onLoadFailed()

        fun onLoadSucceeded()
    }
}
