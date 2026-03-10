package org.odk.collect.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

object CanvasExt {
    fun Canvas.drawPath(
        background: Bitmap,
        path: Path,
        paint: Paint,
        left: Float = 0f,
        top: Float = 0f
    ) {
        drawColor(0xFFAAAAAA.toInt())
        drawBitmap(background, left, top, Paint(Paint.DITHER_FLAG))
        drawPath(path, paint)
    }
}