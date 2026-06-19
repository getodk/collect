package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.image.ImageWidgetAnswer
import org.odk.collect.android.widgets.video.VideoWidgetAnswer
import org.odk.collect.androidshared.system.ContextExt.getActivity
import org.odk.collect.icons.R

@Composable
fun WidgetAnswer(
    modifier: Modifier = Modifier,
    prompt: FormEntryPrompt,
    answer: String?,
    fontSize: Int? = null,
    compact: Boolean = false,
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    if (!answer.isNullOrEmpty()) {
        when (prompt.controlType) {
            Constants.CONTROL_INPUT -> {
                when (prompt.dataType) {
                    Constants.DATATYPE_BARCODE -> TextWidgetAnswer(
                        modifier,
                        ImageVector.vectorResource(R.drawable.ic_baseline_barcode_scanner_white_24),
                        answer,
                        fontSize,
                        if (compact) Arrangement.Start else Arrangement.Center,
                        onClick,
                        onLongClick
                    )
                    else -> TextWidgetAnswer(
                        modifier,
                        null,
                        answer,
                        fontSize,
                        if (compact) Arrangement.Start else Arrangement.Center,
                        onClick,
                        onLongClick
                    )
                }
            }
            Constants.CONTROL_IMAGE_CHOOSE -> ImageWidgetAnswer(
                if (compact) {
                    modifier
                        .height(200.dp)
                        .wrapContentWidth(Alignment.Start)
                } else {
                    modifier.fillMaxWidth()
                },
                answer,
                if (compact) ContentScale.Fit else ContentScale.FillWidth,
                mediaWidgetAnswerViewModel,
                onLongClick
            )
            Constants.CONTROL_VIDEO_CAPTURE -> VideoWidgetAnswer(modifier, answer, mediaWidgetAnswerViewModel, onLongClick)
            Constants.CONTROL_FILE_CAPTURE -> {
                val context = LocalContext.current

                TextWidgetAnswer(
                    modifier,
                    Icons.Default.AttachFile,
                    answer,
                    fontSize,
                    if (compact) Arrangement.Start else Arrangement.Center,
                    { mediaWidgetAnswerViewModel.openFile(context.getActivity(), answer) },
                    onLongClick,
                    stringResource(org.odk.collect.strings.R.string.open_file)
                )
            }
            else -> TextWidgetAnswer(
                modifier,
                null,
                answer,
                fontSize,
                if (compact) Arrangement.Start else Arrangement.Center,
                onClick,
                onLongClick
            )
        }
    }
}
