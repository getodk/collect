package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.androidshared.ui.compose.marginStandard
import org.odk.collect.strings.R.string

@Composable
fun ImageWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    newImagesOnly: Boolean,
    buttonFontSize: Int,
    onCaptureClick: () -> Unit,
    onChooseClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.PhotoCamera,
                stringResource(string.capture_image),
                buttonFontSize,
                onCaptureClick,
                onLongClick
            )
        }

        if (!readOnly && !newImagesOnly) {
            WidgetIconButton(
                Icons.Default.PhotoLibrary,
                stringResource(string.choose_image),
                buttonFontSize,
                onChooseClick,
                onLongClick,
                Modifier
                    .padding(top = marginStandard())
            )
        }

        WidgetAnswer(
            Modifier.padding(top = marginStandard()),
            formEntryPrompt,
            answer,
            mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
            onLongClick = onLongClick
        )
    }
}
