package org.odk.collect.android.widgets.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

@Composable
fun VideoWidgetAnswer(
    modifier: Modifier,
    answer: String,
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current

    val bitmapFlow = remember(answer) { mediaWidgetAnswerViewModel.getFrame(answer, context) }
    val bitmap by bitmapFlow.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = {
                    if (MultiClickGuard.allowClick()) {
                        mediaWidgetAnswerViewModel.openFile(context, answer, "video/*")
                    }
                },
                onLongClick = onLongClick,
                onClickLabel = stringResource(org.odk.collect.strings.R.string.play_video)
            ),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray)
        )

        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(64.dp)
        )
    }
}
