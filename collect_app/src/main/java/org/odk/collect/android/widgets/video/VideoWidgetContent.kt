package org.odk.collect.android.widgets.video

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.strings.R.string

@Composable
fun VideoWidgetContent(
    viewModelProvider: ViewModelProvider,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    newVideoOnly: Boolean,
    buttonFontSize: Int,
    onRecordClick: () -> Unit,
    onChooseClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.Videocam,
                stringResource(string.capture_video),
                buttonFontSize,
                onRecordClick,
                onLongClick
            )
        }

        if (!readOnly && !newVideoOnly) {
            WidgetIconButton(
                Icons.Default.VideoLibrary,
                stringResource(string.choose_video),
                buttonFontSize,
                onChooseClick,
                onLongClick,
                Modifier
                    .padding(top = dimensionResource(id = dimen.margin_standard))
            )
        }

        WidgetAnswer(
            Modifier.padding(top = dimensionResource(id = dimen.margin_standard)),
            formEntryPrompt,
            answer,
            viewModelProvider = viewModelProvider,
            onLongClick = onLongClick
        )
    }
}
