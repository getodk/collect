package org.odk.collect.android.widgets

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.widgets.video.VideoWidgetAnswer
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.strings.R.string

@Composable
fun ExVideoWidgetContent(
    videoUri: Uri?,
    mediaUtils: MediaUtils?,
    readOnly: Boolean,
    onLaunchClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.OpenInNew,
                stringResource(string.launch_app),
                onLaunchClick,
                onLongClick,
                Modifier.testTag("record_video_button")
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
private fun ExVideoWidgetContentPreview() {
    MaterialTheme {
        ExVideoWidgetContent(
            null,
            null,
            false,
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
            null,
            null,
            true,
            {},
            {}
        )
    }
}
