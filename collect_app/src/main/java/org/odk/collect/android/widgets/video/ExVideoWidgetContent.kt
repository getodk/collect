package org.odk.collect.android.widgets.video

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.strings.R.string

@Composable
fun ExVideoWidgetContent(
    readOnly: Boolean,
    fontSize: Int,
    onLaunchClick: () -> Unit,
    onLongClick: () -> Unit,
    widgetAnswer: @Composable () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.AutoMirrored.Filled.OpenInNew,
                stringResource(string.launch_app),
                fontSize,
                onLaunchClick,
                onLongClick
            )
        }

        widgetAnswer()
    }
}

@Preview
@Composable
private fun ExVideoWidgetContentPreview() {
    MaterialTheme {
        ExVideoWidgetContent(
            false,
            10,
            {},
            {},
            {}
        )
    }
}

@Preview
@Composable
private fun ExVideoWidgetContentReadOnlyPreview() {
    MaterialTheme {
        ExVideoWidgetContent(
            true,
            10,
            {},
            {},
            {}
        )
    }
}
