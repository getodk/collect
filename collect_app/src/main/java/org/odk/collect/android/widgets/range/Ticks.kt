package org.odk.collect.android.widgets.range

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
fun Ticks(ticks: Int) {
    repeat(ticks) { index ->
        if (index == 0 || index == ticks - 1) {
            Spacer(modifier = Modifier.size(TICK_WIDTH))
        } else {
            Tick()
        }
    }
}

@Composable
private fun Tick() {
    val sliderTickContentDescription = stringResource(org.odk.collect.strings.R.string.slider_tick)

    Box(
        modifier = Modifier
            .size(TICK_WIDTH)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary)
            .semantics {
                contentDescription = sliderTickContentDescription
            }
    )
}

private val TICK_WIDTH = 4.dp
