package org.odk.collect.material

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview

/**
 * Implementation of "pills" present on the Material 3 website and its examples, but not
 * included in the spec or in Android's MaterialComponents. The pill will use the
 * `?shapeAppearanceCornerSmall` shape appearance for the current theme.
 */
@Composable
fun Pill(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = text
            }
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(
                horizontal = dimensionResource(org.odk.collect.androidshared.R.dimen.margin_small),
                vertical = dimensionResource(
                    org.odk.collect.androidshared.R.dimen.margin_extra_extra_small
                )
            )
    ) {
        Icon(
            modifier = Modifier.size(dimensionResource(org.odk.collect.androidshared.R.dimen.margin_standard)),
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconColor
        )

        Text(
            modifier = Modifier.padding(start = dimensionResource(org.odk.collect.androidshared.R.dimen.margin_extra_small)),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Pill(text = "Text", icon = org.odk.collect.icons.R.drawable.ic_baseline_wifi_off_24)
    }
}
