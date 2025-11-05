package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.odk.collect.strings.R.string

@Composable
fun ArbitraryFileWidgetContent(
    readOnly: Boolean,
    fontSize: Int,
    onChooseFileClick: () -> Unit,
    onLongClick: () -> Unit,
    widgetAnswer: @Composable () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.AttachFile,
                stringResource(string.choose_file),
                fontSize,
                onChooseFileClick,
                onLongClick
            )
        }

        widgetAnswer()
    }
}

@Preview
@Composable
private fun ArbitraryFileWidgetContentPreview() {
    MaterialTheme {
        ArbitraryFileWidgetContent(
            false,
            10,
            {},
            {},
            {}
        )
    }
}
