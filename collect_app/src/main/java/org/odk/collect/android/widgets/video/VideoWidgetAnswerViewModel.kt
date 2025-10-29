package org.odk.collect.android.widgets.video

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.androidshared.utils.loadThumbnail
import org.odk.collect.async.Scheduler
import java.io.File

class VideoWidgetAnswerViewModel(
    private val scheduler: Scheduler,
    private val questionMediaManager: QuestionMediaManager,
    private val mediaUtils: MediaUtils
) : ViewModel() {

    fun getFrame(answer: String?, context: Context): StateFlow<ImageBitmap?> {
        val bitmapState = MutableStateFlow<ImageBitmap?>(null)

        val file = questionMediaManager.getAnswerFile(answer)
        if (file != null) {
            scheduler.immediate {
                val thumbnail = file.loadThumbnail(context)?.asImageBitmap()
                bitmapState.value = thumbnail
            }
        }

        return bitmapState
    }

    fun playVideo(context: Context, file: File) {
        mediaUtils.openFile(context, file, "video/*")
    }
}
