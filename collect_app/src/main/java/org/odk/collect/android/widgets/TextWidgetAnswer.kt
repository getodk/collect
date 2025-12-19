package org.odk.collect.android.widgets

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.sp
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

@Composable
fun TextWidgetAnswer(
    modifier: Modifier,
    icon: ImageVector?,
    answer: String,
    fontSize: Int,
    onLongClick: () -> Unit,
    onClickLabel: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (MultiClickGuard.allowClick()) {
                        onClick()
                    }
                },
                onLongClick = onLongClick,
                onClickLabel = onClickLabel
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.padding(end = dimensionResource(id = dimen.margin_small)),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = dimen.high_emphasis.toFloat()
                )
            )
        }
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = dimen.high_emphasis.toFloat()
                )
            )
        )
    }
}
