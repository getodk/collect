package org.odk.collect.android.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.video.VideoWidgetAnswer

@Composable
fun WidgetAnswer(
    modifier: Modifier = Modifier,
    prompt: FormEntryPrompt,
    answer: String?,
    viewModelProvider: ViewModelProvider,
    onLongClick: () -> Unit = {}
) {
    if (answer != null) {
        when (prompt.controlType) {
            Constants.CONTROL_VIDEO_CAPTURE -> VideoWidgetAnswer.Container(modifier, answer, viewModelProvider, onLongClick)
            else -> throw IllegalArgumentException("Unsupported control type: ${prompt.controlType}")
        }
    }
}
