package org.odk.collect.android.widgets.video

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.strings.R.string

@Composable
fun VideoWidgetContent(
    videoUri: Uri?,
    mediaUtils: MediaUtils?,
    readOnly: Boolean,
    newVideoOnly: Boolean,
    onRecordClick: () -> Unit,
    onChooseClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.Videocam,
                stringResource(string.capture_video),
                onRecordClick,
                onLongClick,
                Modifier.testTag("record_video_button")
            )
        }

        if (!readOnly && !newVideoOnly) {
            Spacer(Modifier.height(dimensionResource(id = dimen.margin_standard)))

            WidgetIconButton(
                Icons.Default.VideoLibrary,
                stringResource(string.choose_video),
                onChooseClick,
                onLongClick,
                Modifier.testTag("choose_video_button")
            )
        }

        if (videoUri != null) {
            Spacer(Modifier.height(dimensionResource(id = dimen.margin_standard)))
            VideoWidgetAnswer(videoUri, mediaUtils)
        }
    }
}

@Preview
@Composable
private fun VideoWidgetContentPreview() {
    MaterialTheme {
        VideoWidgetContent(
            null,
            null,
            false,
            false,
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
            null,
            null,
            true,
            false,
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
            null,
            null,
            false,
            true,
            {},
            {},
            {}
        )
    }
}
