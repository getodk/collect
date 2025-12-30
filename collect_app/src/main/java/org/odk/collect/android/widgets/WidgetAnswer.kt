package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.video.VideoWidgetAnswer
import org.odk.collect.icons.R

@Composable
fun WidgetAnswer(
    modifier: Modifier = Modifier,
    prompt: FormEntryPrompt,
    answer: String?,
    fontSize: Int? = null,
    summaryView: Boolean = false,
    viewModelProvider: ViewModelProvider,
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
                        if (summaryView) Arrangement.Start else Arrangement.Center,
                        onLongClick
                    )
                    else -> TextWidgetAnswer(
                        modifier,
                        null,
                        answer,
                        fontSize,
                        if (summaryView) Arrangement.Start else Arrangement.Center,
                        onLongClick
                    )
                }
            }
            Constants.CONTROL_VIDEO_CAPTURE -> VideoWidgetAnswer(modifier, answer, viewModelProvider, onLongClick)
            Constants.CONTROL_FILE_CAPTURE -> {
                val context = LocalContext.current
                val viewModel = viewModelProvider[MediaWidgetAnswerViewModel::class]

                TextWidgetAnswer(
                    modifier,
                    Icons.Default.AttachFile,
                    answer,
                    fontSize,
                    if (summaryView) Arrangement.Start else Arrangement.Center,
                    onLongClick,
                    stringResource(org.odk.collect.strings.R.string.open_file)
                ) { viewModel.openFile(context, answer) }
            }
            else -> TextWidgetAnswer(
                modifier,
                null,
                answer,
                fontSize,
                if (summaryView) Arrangement.Start else Arrangement.Center,
                onLongClick
            )
        }
    }
}
