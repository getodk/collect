package org.odk.collect.android.widgets.range

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun Tick() {
    val sliderTickContentDescription = stringResource(org.odk.collect.strings.R.string.slider_tick)
    val tickWidth = 4.dp

    Box(
        modifier = Modifier
            .size(tickWidth)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary)
            .semantics {
                contentDescription = sliderTickContentDescription
            }
    )
}
