package org.odk.collect.android.widgets.arbitraryfile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.strings.R.string

@Composable
fun ArbitraryFileWidgetContent(
    viewModelProvider: ViewModelProvider,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    buttonFontSize: Int,
    answerFontSize: Int,
    onChooseFileClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.AttachFile,
                stringResource(string.choose_file),
                buttonFontSize,
                onChooseFileClick,
                onLongClick
            )
        }

        WidgetAnswer(
            Modifier.padding(top = dimensionResource(id = dimen.margin_standard)),
            formEntryPrompt,
            answer,
            answerFontSize,
            viewModelProvider = viewModelProvider,
            onLongClick = onLongClick
        )
    }
}
