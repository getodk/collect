package org.odk.collect.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.DITHER_FLAG
import android.graphics.Path
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.async.Scheduler
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import org.odk.collect.draw.CanvasExt.drawPath

internal class DrawViewModel(
    private val output: File,
    private val scheduler: Scheduler
) : ViewModel() {

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult = _saveResult

    fun save(background: Bitmap, path: Path, paint: Paint) {
        scheduler.immediate(
            background = {
                try {
                    saveImage(background, path, paint)
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

    private fun saveImage(background: Bitmap, path: Path, paint: Paint) {
        val fos = FileOutputStream(output)
        val bitmap = createBitmap(background.width, background.height)
        val canvas = Canvas(bitmap)
        canvas.drawPath(background, path, paint)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)

        try {
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
