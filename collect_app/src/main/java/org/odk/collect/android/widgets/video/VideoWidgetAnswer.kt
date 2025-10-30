package org.odk.collect.android.widgets.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle

object VideoWidgetAnswer {
    @Composable
    fun Container(
        modifier: Modifier,
        answer: String,
        viewModelProvider: ViewModelProvider
    ) {
        val context = LocalContext.current
        val viewModel = viewModelProvider[VideoWidgetAnswerViewModel::class]

        val bitmapFlow = remember(answer) { viewModel.getFrame(answer, context) }
        val bitmap by bitmapFlow.collectAsStateWithLifecycle()

        Content(modifier, bitmap) {
            viewModel.playVideo(context, answer)
        }
    }

    @Composable
    fun Content(
        modifier: Modifier,
        bitmap: ImageBitmap?,
        onPlayClick: () -> Unit
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable { onPlayClick() }
                .testTag("video_widget_answer"),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(org.odk.collect.strings.R.string.play_video),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                )
            }
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}
