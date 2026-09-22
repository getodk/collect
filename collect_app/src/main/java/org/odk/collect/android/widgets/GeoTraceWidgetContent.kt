package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.javarosa.form.api.FormEntryPrompt
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
            WidgetIconButton(
                ImageVector.vectorResource(R.drawable.ic_outline_polyline_white_24),
                if (readOnly) {
                    stringResource(string.view_line)
                } else if (answer.isNullOrEmpty()) {
                    stringResource(string.get_line)
                } else {
                    stringResource(string.view_or_change_line)
                },
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
