package org.odk.collect.android.widgets.arbitraryfile

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.strings.R

@Composable
fun ArbitraryFileWidgetAnswer(
    modifier: Modifier,
    answer: String,
    fontSize: Int,
    viewModelProvider: ViewModelProvider,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModelProvider[ArbitraryFileWidgetAnswerViewModel::class]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (MultiClickGuard.allowClick()) {
                        viewModel.openFile(context, answer)
                    }
                },
                onLongClick = onLongClick,
                onClickLabel = stringResource(R.string.play_video)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AttachFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = dimen.margin_small)))
        Text(
            text = answer,
            style = TextStyle(fontSize = fontSize.sp)
        )
    }
}
