package org.odk.collect.android.widgets.barcode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.icons.R
import org.odk.collect.strings.R.string

@Composable
fun BarcodeWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    isAnswerHidden: Boolean,
    buttonFontSize: Int,
    answerFontSize: Int,
    onGetBarcodeClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                ImageVector.vectorResource(R.drawable.ic_baseline_barcode_scanner_white_24),
                if (answer == null) {
                    stringResource(string.get_barcode)
                } else {
                    stringResource(string.replace_barcode)
                },
                buttonFontSize,
                onGetBarcodeClick,
                onLongClick
            )
        }

        if (!isAnswerHidden) {
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
}
