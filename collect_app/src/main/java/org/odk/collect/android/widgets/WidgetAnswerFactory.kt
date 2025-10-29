package org.odk.collect.android.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.video.VideoWidgetAnswer
import org.odk.collect.android.widgets.video.VideoWidgetAnswerViewModel

@Composable
fun widgetAnswer(
    prompt: FormEntryPrompt,
    context: Context,
    answer: String?,
    questionMediaManager: QuestionMediaManager,
    viewModelProvider: ViewModelProvider
): (@Composable () -> Unit)? = when (prompt.controlType) {
    Constants.CONTROL_VIDEO_CAPTURE -> videoWidgetAnswer(context, answer, questionMediaManager, viewModelProvider)
    else -> throw IllegalArgumentException("Unsupported control type: ${prompt.controlType}")
}

@Composable
private fun videoWidgetAnswer(
    context: Context,
    answer: String?,
    questionMediaManager: QuestionMediaManager,
    viewModelProvider: ViewModelProvider
): (@Composable () -> Unit)? {
    val file = questionMediaManager.getAnswerFile(answer) ?: return null

    val viewModel = viewModelProvider[VideoWidgetAnswerViewModel::class]

    val bitmapFlow = remember(answer) {
        viewModel.getFrame(answer, context)
    }

    return {
        val bitmap by bitmapFlow.collectAsState()

        VideoWidgetAnswer(bitmap) {
            viewModel.playVideo(context, file)
        }
    }
}
