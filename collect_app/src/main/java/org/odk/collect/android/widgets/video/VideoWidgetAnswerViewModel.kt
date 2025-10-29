package org.odk.collect.android.widgets.video

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.androidshared.utils.loadThumbnail
import java.io.File

class VideoWidgetAnswerViewModel(
    private val questionMediaManager: QuestionMediaManager,
    private val mediaUtils: MediaUtils
) : ViewModel() {

    fun init(answer: String?, context: Context): StateFlow<ImageBitmap?> {
        val bitmapState = MutableStateFlow<ImageBitmap?>(null)

        val file = questionMediaManager.getAnswerFile(answer)
        if (file != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val thumbnail = file.loadThumbnail(context)?.asImageBitmap()
                bitmapState.value = thumbnail
            }
        }

        return bitmapState
    }

    fun playVideo(context: Context, file: File) {
        mediaUtils.openFile(context, file, "video/*")
    }

    class Factory(
        private val questionMediaManager: QuestionMediaManager,
        private val mediaUtils: MediaUtils
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VideoWidgetAnswerViewModel(questionMediaManager, mediaUtils) as T
        }
    }
}
