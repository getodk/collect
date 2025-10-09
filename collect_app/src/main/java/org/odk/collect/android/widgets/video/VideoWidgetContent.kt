package org.odk.collect.android.widgets.video

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.odk.collect.android.utilities.MediaUtils
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
            IconButton(
                Icons.Default.Videocam,
                stringResource(string.capture_video),
                onRecordClick,
                onLongClick,
                Modifier.testTag("record_video_button")
            )
        }

        if (!readOnly && !newVideoOnly) {
            Spacer(Modifier.height(dimensionResource(id = dimen.margin_standard)))

            IconButton(
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

@Composable
private fun IconButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true
) {
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = dimensionResource(id = dimen.margin_small)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            tint = contentColor,
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = dimen.margin_extra_small)))
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge
        )
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
