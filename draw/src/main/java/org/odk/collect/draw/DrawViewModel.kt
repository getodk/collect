package org.odk.collect.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.async.Scheduler
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

internal class DrawViewModel(
    private val output: File,
    private val scheduler: Scheduler
) : ViewModel() {

    private val _saveResult = MutableLiveData<Boolean?>(null)
    val saveResult = _saveResult

    fun save(drawView: DrawView) {
        scheduler.immediate(
            background = {
                try {
                    saveImageFromDrawView(drawView)
                    true
                } catch (e: FileNotFoundException) {
                    false
                }
            },
            foreground = { success ->
                _saveResult.value = success
            }
        )
    }

    private fun saveImageFromDrawView(drawView: DrawView) {
        if (drawView.width == 0 || drawView.height == 0) {
            // apparently on 4.x, the orientation change notification can occur
            // sometime before the view is rendered. In that case, the view
            // dimensions will not be known.
            Timber.e(Error("View has zero width or zero height"))
        } else {
            val fos = FileOutputStream(output)
            val bitmap = Bitmap.createBitmap(
                drawView.bitmapWidth,
                drawView.bitmapHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawView.drawOnCanvas(canvas, 0f, 0f)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
            try {
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
