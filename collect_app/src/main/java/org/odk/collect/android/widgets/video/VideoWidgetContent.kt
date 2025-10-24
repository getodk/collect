package org.odk.collect.android.widgets.video

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.strings.R.string

@Composable
fun VideoWidgetContent(
    readOnly: Boolean,
    newVideoOnly: Boolean,
    fontSize: Int,
    onRecordClick: () -> Unit,
    onChooseClick: () -> Unit,
    onLongClick: () -> Unit,
    widgetAnswer: (@Composable () -> Unit)?,
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.Videocam,
                stringResource(string.capture_video),
                fontSize,
                onRecordClick,
                onLongClick,
                Modifier.testTag("record_video_button")
            )
        }

        if (!readOnly && !newVideoOnly) {
            WidgetIconButton(
                Icons.Default.VideoLibrary,
                stringResource(string.choose_video),
                fontSize,
                onChooseClick,
                onLongClick,
                Modifier
                    .testTag("choose_video_button")
                    .padding(top = dimensionResource(id = dimen.margin_standard))
            )
        }

        if (widgetAnswer != null) {
            Spacer(Modifier.height(dimensionResource(id = dimen.margin_standard)))
            widgetAnswer()
        }
    }
}

@Preview
@Composable
private fun VideoWidgetContentPreview() {
    MaterialTheme {
        VideoWidgetContent(
            false,
            false,
            10,
            {},
            {},
            {},
            {}
        )
    }
}

@Preview
@Composable
private fun VideoWidgetContentReadOnlyPreview() {
    MaterialTheme {
        VideoWidgetContent(
            true,
            false,
            10,
            {},
            {},
            {},
            {}
        )
    }
}

@Preview
@Composable
private fun VideoWidgetContentNewVideoOnlyPreview() {
    MaterialTheme {
        VideoWidgetContent(
            false,
            true,
            10,
            {},
            {},
            {},
            {}
        )
    }
}
