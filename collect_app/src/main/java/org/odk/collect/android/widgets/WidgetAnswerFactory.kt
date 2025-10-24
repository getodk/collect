package org.odk.collect.android.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.video.VideoWidgetAnswer

@Composable
fun widgetAnswer(
    prompt: FormEntryPrompt,
    context: Context,
    answer: String?,
    questionMediaManager: QuestionMediaManager,
    mediaUtils: MediaUtils
): (@Composable () -> Unit)? = when (prompt.controlType) {
    Constants.CONTROL_VIDEO_CAPTURE -> videoWidgetAnswer(context, answer, questionMediaManager, mediaUtils)
    else -> throw IllegalArgumentException("Unsupported control type: ${prompt.controlType}")
}

@Composable
private fun videoWidgetAnswer(
    context: Context,
    answer: String?,
    questionMediaManager: QuestionMediaManager,
    mediaUtils: MediaUtils
): (@Composable () -> Unit)? {
    val file = questionMediaManager.getAnswerFile(answer) ?: return null
    val videoUri = file.toUri()

    return {
        val bitmap by produceState<ImageBitmap?>(initialValue = null, videoUri) {
            value = withContext(Dispatchers.IO) {
                mediaUtils.getVideoFrame(context, videoUri)?.asImageBitmap()
            }
        }

        VideoWidgetAnswer(bitmap) {
            mediaUtils.openFile(context, videoUri.toFile(), "video/*")
        }
    }
}
