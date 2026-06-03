package org.odk.collect.android.widgets

import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    contentScale: ContentScale,
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val imageFile = remember(answer) { mediaWidgetAnswerViewModel.getImage(answer) }
    var isError by remember(answer) { mutableStateOf(false) }

    imageFile?.let {
        if (isError) {
            Text(
                modifier = modifier,
                text = stringResource(org.odk.collect.strings.R.string.selected_invalid_image),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            AsyncImage(
                model = it,
                contentScale = contentScale,
                contentDescription = null,
                onError = { isError = true },
                modifier = modifier
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
}
