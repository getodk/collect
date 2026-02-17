package org.odk.collect.android.widgets.arbitraryfile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.strings.R.string

@Composable
fun ExArbitraryFileWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    buttonFontSize: Int,
    answerFontSize: Int,
    onLaunchClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.AutoMirrored.Filled.OpenInNew,
                stringResource(string.launch_app),
                buttonFontSize,
                onLaunchClick,
                onLongClick
            )
        }

        WidgetAnswer(
            Modifier.padding(top = dimensionResource(id = dimen.margin_standard)),
            formEntryPrompt,
            answer,
            answerFontSize,
            mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
            onLongClick = onLongClick
        )
    }
}
