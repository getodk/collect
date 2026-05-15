package org.odk.collect.android.widgets

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

@Composable
fun ImageWidgetAnswer(
    modifier: Modifier,
    answer: String,
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val imageFile = remember(answer) { mediaWidgetAnswerViewModel.getImage(answer) }

    imageFile?.let {
        AsyncImage(
            model = it,
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = modifier
                .wrapContentWidth(Alignment.Start)
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(
                    onClick = {
                        if (MultiClickGuard.allowClick()) {
                            mediaWidgetAnswerViewModel.openFile(context, answer, "image/*")
                        }
                    },
                    onLongClick = onLongClick,
                    onClickLabel = stringResource(org.odk.collect.strings.R.string.open_file)
                )
        )
    }
}
