package org.odk.collect.draw

import android.graphics.Bitmap
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

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult = _saveResult

    fun save(background: Bitmap) {
        scheduler.immediate(
            background = {
                try {
                    saveImage(background)
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

    private fun saveImage(bitmap: Bitmap) {
        val fos = FileOutputStream(output)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)

        try {
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
