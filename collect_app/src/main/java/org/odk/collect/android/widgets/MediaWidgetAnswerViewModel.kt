package org.odk.collect.android.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.utilities.getExistingAnswerFile
import org.odk.collect.androidshared.utils.getVideoThumbnail
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.MapPreviewRenderer
import org.odk.collect.maps.traces.TraceDescription
import java.io.File

class MediaWidgetAnswerViewModel(
    private val scheduler: Scheduler,
    private val questionMediaManager: QuestionMediaManager,
    private val mediaUtils: MediaUtils,
    private val mapPreviewRenderer: MapPreviewRenderer
) : ViewModel() {
    fun getFrame(answer: String?, context: Context): StateFlow<ImageBitmap?> {
        val bitmapState = MutableStateFlow<ImageBitmap?>(null)

        val file = questionMediaManager.getExistingAnswerFile(answer)
        if (file != null) {
            scheduler.immediate {
                val thumbnail = file.getVideoThumbnail(context)?.asImageBitmap()
                bitmapState.value = thumbnail
            }
        }

        return bitmapState
    }

    fun getImage(answer: String?): File? {
        return questionMediaManager.getExistingAnswerFile(answer)
    }

    fun renderMapPreview(
        trace: TraceDescription,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ): () -> Unit {
        return mapPreviewRenderer.render(trace, width, height, callback)
    }

    fun openFile(activity: Activity, answer: String?, mimeType: String? = null) {
        val file = questionMediaManager.getExistingAnswerFile(answer)
        if (file != null) {
            mediaUtils.openFile(activity, file, mimeType)
        }
    }
}
