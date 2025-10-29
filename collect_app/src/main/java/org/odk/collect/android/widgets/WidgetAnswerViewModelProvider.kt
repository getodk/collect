package org.odk.collect.android.widgets

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.video.VideoWidgetAnswerViewModel

class WidgetAnswerViewModelProvider(
    owner: ComponentActivity,
    questionMediaManager: QuestionMediaManager,
    mediaUtils: MediaUtils
) : ViewModelProvider(
        owner,
        WidgetAnswerViewModelFactory(questionMediaManager, mediaUtils)
    )

private class WidgetAnswerViewModelFactory(
    private val questionMediaManager: QuestionMediaManager,
    private val mediaUtils: MediaUtils
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(VideoWidgetAnswerViewModel::class.java) -> VideoWidgetAnswerViewModel(questionMediaManager, mediaUtils) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
