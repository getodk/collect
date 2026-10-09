package org.odk.collect.android.widgets.geo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.ui.compose.marginStandard
import org.odk.collect.icons.R
import org.odk.collect.strings.R.string

@Composable
fun GeoTraceWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    buttonFontSize: Int,
    answerFontSize: Int,
    onGetLineClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly || !answer.isNullOrEmpty()) {
            val buttonText = when {
                readOnly -> stringResource(string.view_line)
                answer.isNullOrEmpty() -> stringResource(string.get_line)
                else -> stringResource(string.view_or_change_line)
            }
            WidgetIconButton(
                ImageVector.vectorResource(R.drawable.ic_outline_polyline_white_24),
                buttonText,
                buttonFontSize,
                onGetLineClick,
                onLongClick
            )
        }

        WidgetAnswer(
            Modifier.padding(top = marginStandard()),
            formEntryPrompt,
            answer,
            answerFontSize,
            mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
            onLongClick = onLongClick
        )
    }
}
