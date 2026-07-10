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
package org.odk.collect.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withTranslation
import org.odk.collect.androidshared.bitmap.ImageFileUtils
import org.odk.collect.androidshared.utils.calculateSampleSize
import java.io.File
import javax.inject.Inject

class DrawView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    init {
        (context.applicationContext as DrawDependencyComponentProvider)
            .drawDependencyComponent.inject(this)
    }

    private lateinit var bitmap: Bitmap

    @Inject
    lateinit var imagePath: String

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 10f
    }

    private var canvas = Canvas()
    private var currentPath = Path()
    private var valueX = 0f
    private var valueY = 0f

    val bitmapHeight: Int
        get() = bitmap.height

    val bitmapWidth: Int
        get() = bitmap.width

    private val displayScale: Float
        get() = minOf(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)

    // Centered horizontally
    private val displayLeft: Float
        get() = (width - bitmap.width * displayScale) / 2f

    // Centered vertically
    private val displayTop: Float
        get() = (height - bitmap.height * displayScale) / 2f

    private var isSignature = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (!::bitmap.isInitialized) {
            resetImage(w, h)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(0xFFAAAAAA.toInt())
        canvas.withTranslation(displayLeft, displayTop) {
            scale(displayScale, displayScale)
            drawBitmap(bitmap, 0f, 0f, Paint(Paint.DITHER_FLAG))
            drawPath(currentPath, paint)
        }
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

    private fun toBitmapX(x: Float) = (x - displayLeft) / displayScale
    private fun toBitmapY(y: Float) = (y - displayTop) / displayScale

    private fun touchStart(x: Float, y: Float) {
        paint.strokeWidth = 10f / displayScale
        currentPath.reset()
        currentPath.moveTo(toBitmapX(x), toBitmapY(y))
        valueX = x
        valueY = y
    }

    private fun touchMove(x: Float, y: Float) {
        currentPath.quadTo(
            toBitmapX(valueX),
            toBitmapY(valueY),
            toBitmapX((x + valueX) / 2),
            toBitmapY((y + valueY) / 2)
        )
        valueX = x
        valueY = y
    }

    private fun touchUp() {
        if (currentPath.isEmpty) {
            canvas.drawPoint(toBitmapX(valueX), toBitmapY(valueY), paint)
        } else {
            currentPath.lineTo(toBitmapX(valueX), toBitmapY(valueY))

            // commit the path to our offscreen
            canvas.drawPath(currentPath, paint)
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

    private fun resetImage(width: Int, height: Int) {
        val backgroundBitmapFile = File(imagePath)
        if (backgroundBitmapFile.exists()) {
            val options = BitmapFactory.Options()
            options.inSampleSize = backgroundBitmapFile.calculateSampleSize(4096)

            bitmap = ImageFileUtils.getBitmap(backgroundBitmapFile.absolutePath, options)!!
                .copy(Bitmap.Config.ARGB_8888, true)
            canvas = Canvas(bitmap)
        } else {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            if (isSignature) {
                drawLine()
            }
        }
    }
}
