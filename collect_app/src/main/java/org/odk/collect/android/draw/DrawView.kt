/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.utilities.ImageFileUtils
import java.io.File

class DrawView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var bitmap: Bitmap

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 10f
    }

    private var canvas = Canvas()
    private var currentPath = Path()
    private var offscreenPath = Path()
    private var valueX = 0f
    private var valueY = 0f

    val bitmapHeight: Int
        get() = bitmap.height

    val bitmapWidth: Int
        get() = bitmap.width

    // Centered horizontally
    private val bitmapLeft: Int
        get() = (width - bitmap.width) / 2

    // Centered vertically
    private val bitmapTop: Int
        get() = (height - bitmap.height) / 2

    private var isSignature = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetImage(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        drawOnCanvas(canvas, bitmapLeft.toFloat(), bitmapTop.toFloat())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    fun setupView(isSignature: Boolean) {
        this.isSignature = isSignature
    }

    fun reset() {
        val metrics = resources.displayMetrics
        resetImage(metrics.widthPixels, metrics.heightPixels)
    }

    fun setColor(color: Int) {
        paint.color = color
    }

    fun drawOnCanvas(canvas: Canvas, left: Float, top: Float) {
        canvas.drawColor(0xFFAAAAAA.toInt())
        canvas.drawBitmap(bitmap, left, top, Paint(Paint.DITHER_FLAG))
        canvas.drawPath(currentPath, paint)
    }

    private fun touchStart(x: Float, y: Float) {
        currentPath.reset()
        currentPath.moveTo(x, y)
        offscreenPath.reset()
        offscreenPath.moveTo(x - bitmapLeft, y - bitmapTop)
        valueX = x
        valueY = y
    }

    private fun touchMove(x: Float, y: Float) {
        currentPath.quadTo(valueX, valueY, (x + valueX) / 2, (y + valueY) / 2)
        offscreenPath.quadTo(
            valueX - bitmapLeft, valueY - bitmapTop,
            (x + valueX) / 2 - bitmapLeft, (y + valueY) / 2 - bitmapTop
        )
        valueX = x
        valueY = y
    }

    private fun touchUp() {
        if (currentPath.isEmpty) {
            canvas.drawPoint(valueX, valueY, paint)
        } else {
            currentPath.lineTo(valueX, valueY)
            offscreenPath.lineTo(valueX - bitmapLeft, valueY - bitmapTop)

            // commit the path to our offscreen
            canvas.drawPath(offscreenPath, paint)
        }
        // kill this so we don't double draw
        currentPath.reset()
    }

    private fun drawLine() {
        val originalColor = paint.color
        paint.color = Color.BLACK
        canvas.drawLine(
            0f,
            (canvas.height * .7).toFloat(),
            canvas.width.toFloat(),
            (canvas.height * .7).toFloat(),
            paint
        )
        paint.color = originalColor
    }

    private fun resetImage(w: Int, h: Int) {
        val backgroundBitmapFile = File(StoragePathProvider().getTmpImageFilePath())
        if (backgroundBitmapFile.exists()) {
            bitmap = ImageFileUtils.getBitmapScaledToDisplay(backgroundBitmapFile, h, w, true)!!.copy(Bitmap.Config.ARGB_8888, true)
            canvas = Canvas(bitmap)
        } else {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            if (isSignature) {
                drawLine()
            }
        }
    }
}
