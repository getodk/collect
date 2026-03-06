package org.odk.collect.android.widgets.range

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun ValueLabel(
    value: String,
    modifier: Modifier = Modifier
) {
    val currentSliderValueContentDescription = stringResource(org.odk.collect.strings.R.string.current_slider_value)

    Text(
        text = value,
        modifier = modifier.semantics {
            contentDescription = currentSliderValueContentDescription
        },
        style = MaterialTheme.typography.headlineSmall
    )
}
