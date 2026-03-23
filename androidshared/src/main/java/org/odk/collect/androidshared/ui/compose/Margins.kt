package org.odk.collect.androidshared.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import org.odk.collect.androidshared.R.dimen

@Composable
fun marginStandard(): Dp {
    return dimensionResource(id = dimen.margin_standard)
}

@Composable
fun marginSmall(): Dp {
    return dimensionResource(id = dimen.margin_small)
}

@Composable
fun marginExtraSmall(): Dp {
    return dimensionResource(id = dimen.margin_extra_small)
}